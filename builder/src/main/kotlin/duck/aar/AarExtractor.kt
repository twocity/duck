package duck.aar

import java.nio.file.Path

interface AarExtractor {
  fun extract(aar: Path): Aar
}