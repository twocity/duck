package duck

import duck.JavaVersion.JAVA_7
import duck.Logger.Level.VERBOSE
import duck.Project.Companion.APT_DIR
import duck.Project.Companion.JAVAC_OUTPUT

class JavaCompileOptionBuilder(private val context: ProjectContext) {

  fun build(): JavaCompileOptions {
    val androidSdk = context.androidSdk
    val project = context.project
    val bootstrap = androidSdk.frameworkJar.toString()
    val classpath = context.dependencyManager.classPaths()
    context.dependencyManager.jarDeps.map { it.toString() }
    val processorClassPath =
        context.fileFinder.find(
            project.annotationProcessorOptions.jars).map { it.absolutePath }
    return JavaCompileOptions(
        bootstrapClasspath = bootstrap,
        processorClassNames = project.annotationProcessorOptions.processorClassNames,
        processorClasspath = processorClassPath.toList(),
        processorArguments = project.annotationProcessorOptions.arguments,
        generatedCodePath = project.buildDir.resolve(APT_DIR),
        classPath = classpath.map { it.toString() },
        javacOutputPath = project.buildDir.resolve(JAVAC_OUTPUT),
        encoding = "UTF-8",
        source = JAVA_7,
        target = JAVA_7,
        verbose = context.logger.logLevel() == VERBOSE
    )
  }
}