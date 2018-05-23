package duck

import okio.Okio
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit

fun List<String>.run(): Boolean {
  val process = ProcessBuilder()
      .redirectError(Redirect.INHERIT)
      .command(toList())
      .start()
  return process.waitFor() == 0
}

fun List<String>.runThenGet(timeout: Long = 3000): String {
  val process = ProcessBuilder()
      .command(this)
      .redirectOutput(Redirect.PIPE)
      .start().apply {
        waitFor(timeout, TimeUnit.MILLISECONDS)
      }
  val output = Okio.buffer(Okio.source(process.inputStream))
  return output.readByteString().utf8()
}

fun main(args: Array<String>) {
  listOf("echo hello").run()
}