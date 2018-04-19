package duck

import java.io.File

interface FileFinder {
  fun find(patterns: List<String>): Sequence<File>
}
