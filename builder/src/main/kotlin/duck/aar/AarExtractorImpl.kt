package duck.aar

import duck.Logger
import duck.exists
import duck.extractTo
import duck.mkdirs
import okio.ByteString
import java.io.FileNotFoundException
import java.nio.file.Path

class AarExtractorImpl(private val logger: Logger, private val outputDir: Path) : AarExtractor {

  override fun extract(aar: Path): Aar {
    if (!aar.exists()) throw FileNotFoundException("$aar not found.")
    val fileName = ByteString.encodeUtf8(aar.fileName.toString()).md5().hex()
    val file = outputDir.resolve(fileName)
    if (!file.exists()) {
      file.mkdirs()
      logger.verbose("extract: $aar to $file")
      aar.toFile().extractTo(file)
    }
    return AarImpl.from(file, aar.fileName.toString())
  }
}