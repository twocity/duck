package duck.resources

import com.android.builder.symbols.RGeneration
import com.android.builder.symbols.SymbolIo
import com.android.builder.symbols.SymbolUtils
import duck.Project
import duck.Project.Companion
import duck.ProjectContext
import duck.clean
import duck.deleteRecursively
import duck.manifest.ManifestMerger
import duck.mkdirs
import duck.resources.ResourcesLinker.AaptLinkOptions
import java.nio.file.Path

class ResourcesProcessor(private val context: ProjectContext) {

  private val logger = context.logger
  private val androidConfig = context.project.androidConfig
  private val buildDir = context.project.buildDir
  private val tmpDir: Path = buildDir.resolve(".tmp").clean()
  private val aapt: Path = context.androidSdk.aapt
  private val primaryManifest: Path = context.project.manifest

  fun merge(): MergedAndroidData {
    logger.info("Start resource merging...")
    val assetsMerger = AssetsMerger(logger, collectAssets())
    val manifestMerger = ManifestMerger.create(logger, tmpDir) {
      mainManifest = primaryManifest.toFile()
      deps = context.manifestDepsProvider.manifests()
      packageName = androidConfig.applicationId
      minSdkVersion = androidConfig.minSdkVersion
      targetSdkVersion = androidConfig.targetSdkVersion
      versionCode = androidConfig.versionCode
      versionName = androidConfig.versionName
    }
    val resourcesMerger = ResourcesMerger(logger = logger,
        resourcesPaths = collectResources(),
        workingDir = tmpDir.resolve("res").mkdirs(),
        minSdkVersion = androidConfig.minSdkVersion)
    val outputManifest = tmpDir.resolve("manifest/merged.xml")
    val outputAssetsPath = tmpDir.resolve("assets")
    val outputResPath = tmpDir.resolve("res/merged")

    // merging
    return MergedAndroidData(
        mergedResources = resourcesMerger.merge(outputResPath),
        mergedAssets = assetsMerger.merge(outputAssetsPath),
        mergedManifest = manifestMerger.merge(outputManifest)
    )
  }

  fun compile(mergedData: MergedAndroidData): CompiledResources {
    // compiling
    val compiledResOutput = tmpDir.resolve("res/compiled")
    val compiler = ResourcesCompiler(logger, aapt)
    return CompiledResources(
        resources = compiler.compile(mergedData.mergedResources, compiledResOutput),
        assets = mergedData.mergedAssets,
        manifest = mergedData.mergedManifest
    )
  }

  fun link(compiledResources: CompiledResources): LinkedResources {
    val linkedDir = tmpDir.resolve("res/link/")
    linkedDir.clean()
    val resourceApkOutput = linkedDir.resolve("resources.ap_")
    val symbolsOutput = linkedDir.resolve("R.txt")
    val javaSourceOutput = linkedDir.resolve("java")
    val aaptOptions = AaptLinkOptions.build(aapt) {
      androidJar = context.androidSdk.frameworkJar
      resourcesApkOutput = resourceApkOutput
      // not used
      javaSourceDir = javaSourceOutput
      this.symbolsOutput = symbolsOutput
    }
    val linker = ResourcesLinker(logger, aaptOptions)
    linker.link(compiledResources)
    return LinkedResources(resourceApkOutput, symbolsOutput, javaSourceOutput, aaptOptions)
  }

  private fun generateRSourceCode(linked: LinkedResources) {
    logger.info("Generating R.java files...")
    generateRSources(linked.symbolsOutput, androidConfig.applicationId,
        collectDependencySymbolTables(), linked.javaSourceOutput)
  }

  fun packageOutput(@Suppress("UNUSED_PARAMETER") mergedData: MergedAndroidData,
      compiledResources: CompiledResources,
      linkedResources: LinkedResources): ResourcesOutput {
    generateRSourceCode(linkedResources)

    val outputDir = buildDir.resolve(Project.RES_OUTPUT)
    outputDir.clean()

    val manifest = outputDir.resolve("AndroidManifest.xml")
    val resourcesApk = outputDir.resolve("resources.ap_")
    val primaryRTxt = outputDir.resolve("R.txt")
    val sourceCodeDirectory = outputDir.resolve("source/generated/r")
    compiledResources.manifest.toFile().copyTo(manifest.toFile(), true)
    linkedResources.resourcesApk.toFile().copyTo(resourcesApk.toFile())
    linkedResources.symbolsOutput.toFile().copyTo(primaryRTxt.toFile())
    linkedResources.javaSourceOutput.toFile().copyRecursively(sourceCodeDirectory.toFile())

    if (!context.debug) {
      // remove temp files
      tmpDir.deleteRecursively()
    }
    return ResourcesOutput(androidManifest = manifest,
        resourcesApk = resourcesApk,
        primaryRTxt = primaryRTxt,
        sourcesDirectory = sourceCodeDirectory)
  }

  private fun generateRSources(rText: Path, mainPackageName: String,
      depsRTxt: List<Path>, output: Path) {

    val mainSymbol = SymbolIo.readFromAapt(rText.toFile(), mainPackageName)
    val depSymbolTables = SymbolUtils.loadDependenciesSymbolTables(
        depsRTxt.map { it.toFile() }.toSet(), mainPackageName
    )

    RGeneration.generateRForLibraries(mainSymbol, depSymbolTables, output.toFile(), true)
  }


  private fun collectAssets(): List<Path> {
    return mutableListOf<Path>().apply {
      // order matters
      addAll(androidConfig.assetsFolders.map {
        context.project.projectDir.resolve(it)
      })
      addAll(context.dependencyManager.aarDeps.mapNotNull { it.assets().orElse(null) })
    }.toList()
  }

  private fun collectResources(): List<Path> {
    return mutableListOf<Path>().apply {
      addAll(androidConfig.resDirs.map { context.project.projectDir.resolve(it) })
      addAll(context.dependencyManager.aarDeps.map { it.res() })
    }.toList()
  }

  private fun collectDependencySymbolTables(): List<Path> {
    return context.dependencyManager.aarDeps
        .map { it.packageAwareRTxt() }
  }
}