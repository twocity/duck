package duck.dex

import com.android.dx.command.dexer.Main
import duck.DuckException
import duck.Logger
import duck.Logger.Level.VERBOSE
import duck.exists
import java.nio.file.Path

class Dexing(private val logger: Logger, private val filesToDex: Iterable<Path>) {

  fun dex(output: Path): Path {
    logger.info("Dexing...")
    val result = Main.run(dxArguments(output))
    if (result != 0) {
      throw DuckException("Dexing failed")
    }
    require(output.exists()) { "$output not exists after dex" }
    logger.info("Dexed -> $output")
    return output
  }

  private fun dxArguments(outputDex: Path): Main.Arguments {
    val args = Main.Arguments()
    with(args) {
      outName = outputDex.toFile().toString()
      humanOutName = outputDex.toFile().toString()
      fileNames = filesToDex.map { it.toString() }.toTypedArray()
      verbose = logger.logLevel() == VERBOSE
      statistics = logger.logLevel() == VERBOSE
      numThreads = 8
    }
    return args
  }
}