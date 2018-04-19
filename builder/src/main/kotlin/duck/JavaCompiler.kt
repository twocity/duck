package duck

import java.io.File
import java.nio.file.Path
import java.util.Locale
import javax.tools.DiagnosticListener
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.text.Charsets.UTF_8

class JavaCompiler(private val project: Project,
    private val logger: Logger,
    private val diagnostic: DiagnosticListener<JavaFileObject>,
    private val option: JavaCompileOptions) {

  fun compile(files: Iterable<File>): Path {
    logger.info("Compiling java sources")
    val success = defaultJavaCompile(files)
    if (!success) {
      throw DuckException("Java compile failed")
    }

    return packageJar().apply {
      logger.info("Java sources compiled -> $this")
      // remove classes folder
      option.javacOutputPath.deleteRecursively()
    }
  }

  private fun defaultJavaCompile(files: Iterable<File>): Boolean {
    val compiler = ToolProvider.getSystemJavaCompiler()
    val fileManager = compiler.getStandardFileManager(diagnostic, Locale.US, UTF_8)
    fileManager.use {
      val inputFiles = fileManager.getJavaFileObjects(*files.toList().toTypedArray())
      val args = option.asCompilerArgs()
      val task = compiler.getTask(null, fileManager, diagnostic, args, null,
          inputFiles)
      logger.verbose("compiler args: ${args.joinToString(separator = " ")}")
      return task.call()
    }
  }

  private fun packageJar(): Path {
    val classJar = project.buildDir.resolve("classes.jar")
    if (!listOf("jar", "-cf", classJar.toString(), "-C", "${option.javacOutputPath}", ".")
            .run()) {
      throw DuckException("Cant't create jar from classes(${option.javacOutputPath})")
    }
    return classJar
  }
}