package duck

import java.io.File
import java.nio.file.Path

class SourceCodeManager(private val fileFinder: FileFinder,
    private val srcDirsPatterns: List<String>) {
  private fun sourceFiles(): List<File> {
    return fileFinder.find(srcDirsPatterns).toList()
  }

  private val customSourcePaths = mutableListOf<Path>()

  fun addSourcePath(path: Path) {
    customSourcePaths.add(path)
  }

  private fun generatedSourceFiles(): List<File> {
    val pattern = customSourcePaths.map { "$it/**/*.java" }
    return fileFinder.find(pattern).toList()
  }

  fun sources(): List<File> {
    return mutableListOf<File>().apply {
      addAll(sourceFiles())
      addAll(generatedSourceFiles())
    }.toList()
  }
}