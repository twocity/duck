package duck.resources

import duck.DuckException
import duck.Logger
import duck.clean
import duck.exists
import duck.run
import java.nio.file.Path

class ResourcesCompiler(private val logger: Logger,
    private val aapt: Path) {

  fun compile(resources: Path, outputDir: Path): List<Path> {
    outputDir.clean()
    return resources.toFile().walk()
        .filter { it.isFile && !it.isHidden }
        .toList()
        .map { compileFile(it.toPath(), outputDir) }
  }

  private fun compileFile(file: Path, outputDir: Path): Path {
    /**
     * aapt2 compile --legacy \
     * -o output \
     * input file
     */
    val commands = listOf(aapt.toString(), "compile", "--legacy", "-o",
        outputDir.toString(),
        file.toString())
    logger.verbose(commands.subList(1, commands.size).joinToString(separator = " "))
    // compile
    if (!commands.run()) {
      throw DuckException("compile $file failed")
    }
    // find the compiled file
    val type = file.parent.fileName.toString()
    var filename = file.fileName.toString()
    if (type.startsWith("values")) {
      filename = (if (filename.indexOf('.') != -1) filename.substring(0,
          filename.indexOf('.')) else filename) + ".arsc"
    }
    val compiledFile = outputDir.resolve("${type}_$filename.flat")
    if (!compiledFile.exists()) {
      throw DuckException("$compiledFile not found after resources compiled")
    }
    return compiledFile
  }
}