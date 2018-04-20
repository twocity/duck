package duck.aar

import java.nio.file.Path

/**
 * An interface to extract Android Aar file as [Aar]
 */
interface AarExtractor {
  fun extract(aar: Path): Aar
}