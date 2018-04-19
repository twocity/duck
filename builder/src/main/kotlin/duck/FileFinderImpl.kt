package duck

import java.io.File
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.PathMatcher

class FileFinderImpl(private val fileSystem: FileSystem,
    private val projectDir: Path) : FileFinder {
  private val cache by lazy { LinkedHashMap<String, PathMatcher>() }

  override
  fun find(patterns: List<String>): Sequence<File> {
    val matcher = findPathMatcher(patterns)
    return projectDir.toFile().walk().filter {
      it.isFile && matcher.matches(it.toPath())
    }
  }

  private fun findPathMatcher(patterns: List<String>): PathMatcher {
    val key = patterns.joinToString(separator = ":")
    return cache[key] ?: buildPathMatcher(patterns).also {
      cache[key] = it
    }
  }

  private fun buildPathMatcher(patterns: List<String>): PathMatcher {
    val matcherList = patterns.map {
      val path = projectDir.resolve(it)
      fileSystem.getPathMatcher("glob:$path")
    }
    return DelegatePathMatcher(matcherList)
  }

}