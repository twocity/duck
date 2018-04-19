@file:JvmName("Duck")

package duck

import com.google.common.base.Stopwatch
import duck.JavaResMerger.JarJavaRes
import duck.Logger.Level.VERBOSE
import duck.dex.Dexing
import duck.resources.ResourcesProcessor
import java.nio.file.Path

fun main(args: Array<String>) {
  val options = BuildOptions.parse(args)
  val project = Project.from(options.buildFile)

  val context = ProjectContext(project)
  if (options.verbose) context.logger.setLogLevel(VERBOSE)
  build(context)
}

private fun build(context: ProjectContext) {
  val project = context.project
  // cleanup
  clean(project)

  val stopWatch = Stopwatch.createStarted()
  context.dependencyManager.configure()
  /**
   * Step1. Process Android Resources
   */
  val resourcesProcessor = ResourcesProcessor(context)

  /**
   * Step1.1 Merge Android Resources:
   * + AndroidManifest.xml
   * + assets
   * + res
   */
  val merged = resourcesProcessor.merge()

  /**
   * Step1.2 Compile merged res by aapt2, see `aapt2 compile -h`
   * This step will generate *.flat files
   */
  val compiled = resourcesProcessor.compile(merged)

  /**
   * Step1.3 Link compiled res, assets, manifest..., see `aapt2 link -h`
   * This step will generate: R.txt, R.java, resources.ap_
   */
  val linked = resourcesProcessor.link(compiled)

  /**
   * Step1.4 Generate R.java for dependency libraries, cleanup temp dirs...
   */
  val resourcesOutput = resourcesProcessor.packageOutput(merged, compiled, linked)


  /**
   * Step2. Compile java sources with generated R.java from Step1.4
   */
  context.sourceCodeManager.addSourcePath(resourcesOutput.sourcesDirectory)
  val javaCompileOption = JavaCompileOptionBuilder(context).build()
  val javaCompiler = JavaCompiler(project, context.logger, SimpleDiagnosticListener(context.logger),
      javaCompileOption)
  val jar = javaCompiler.compile(context.sourceCodeManager.sources())


  /**
   * Step3. Merge Java Resources
   */
  val jarRes = context.dependencyManager.jarDeps.map { JarJavaRes.fromJar(it) }
  val jarResMerger = JavaResMerger(context.logger, jarRes)
  val mergedJavaResZip = jarResMerger.merge(project.buildDir.resolve("javaRes.zip"))


  /**
   * Step4. Convert classes.jar to Android Virtual Machine bytecode: classes.dex
   */
  val filesToDex = context.dependencyManager.jarDeps.toMutableList().apply { add(jar) }
  val dexer = Dexing(context.logger, filesToDex)
  val dexFile = dexer.dex(project.buildDir.resolve("classes.dex"))


  /**
   * Step5. Build final APK with:
   * + resources.ap_
   * + classes.dex
   * + javaRes.zip
   */
  context.logger.info("Building Android apk")
  val apkBuilder = ApkBuilder(project)
  apkBuilder.androidResources(resourcesOutput.resourcesApk)
  apkBuilder.dex(dexFile)
  apkBuilder.javaResources(mergedJavaResZip)
  apkBuilder.build()

  context.logger.info("Output: ${project.outputApk}")
  stopWatch.stop()
  context.logger.info("SUCCESS in $stopWatch")
}

private fun clean(project: Project) {
  project.buildDir.deleteRecursively()
}

data class BuildOptions(val buildFile: Path, val verbose: Boolean) {
  companion object {
    fun parse(args: Array<String>): BuildOptions {
      val path = args[0].toPath()
      if (!path.exists()) {
        throw DuckException("$path not found")
      }
      val verbose =
          if (args.size >= 2) args[1] == "--verbose" else false
      return BuildOptions(path, verbose)
    }
  }
}
