package duck

import java.nio.file.Path


data class JavaCompileOptions(
    val bootstrapClasspath: String,
    val processorClassNames: List<String>,
    val processorClasspath: List<String>,
    val processorArguments: Map<String, String>,
    val generatedCodePath: Path,
    val classPath: List<String>,
    val javacOutputPath: Path,
    val encoding: String,
    val source: JavaVersion,
    val target: JavaVersion,
    val verbose:Boolean = false
) {
  fun asCompilerArgs(): List<String> {
    val args = mutableListOf<String>()
    with(args) {
      add("-encoding")
      add(encoding)
      add("-source")
      add(source.v)
      add("-target")
      add(target.v)
      // bootstrap
      add("-bootclasspath")
      add(bootstrapClasspath)

      // classpath
      add("-classpath")
      add(classPath.joinToString(separator = ":"))

      // annotation processor
      if (processorClassNames.isNotEmpty()) {
        add("-processor")
        add(processorClassNames.joinToString(separator = ","))
      }
      if (processorClasspath.isNotEmpty()) {
        add("-processorpath")
        add(processorClasspath.joinToString(separator = ":"))
      }
      add("-s")
      generatedCodePath.mkdirs()
      add(generatedCodePath.toString())
      if (processorArguments.isNotEmpty()) {
        add(processorArguments.toList().joinToString(separator = ".",prefix = "-A") { "${it.first}=${it.second}" })
      }

      // output directory
      add("-d")
      javacOutputPath.mkdirs()
      add(javacOutputPath.toString())
    }

    return args
  }
}
