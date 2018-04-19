package duck

import java.lang.ProcessBuilder.Redirect

fun List<String>.run(): Boolean {
  val process = ProcessBuilder()
      .redirectError(Redirect.INHERIT)
      .command(toList())
      .start()
  return process.waitFor() == 0
}
