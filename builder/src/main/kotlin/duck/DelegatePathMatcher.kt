package duck

import java.nio.file.Path
import java.nio.file.PathMatcher

class DelegatePathMatcher(private val delegates: List<PathMatcher>) : PathMatcher {
  override fun matches(path: Path): Boolean {
    return delegates.any { it.matches(path) }
  }
}