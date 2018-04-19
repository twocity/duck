@file:Suppress("unused")

package duck

import duck.Logger.Level
import duck.Logger.Level.VERBOSE
import java.io.PrintWriter
import java.io.StringWriter

class LoggerImpl : Logger {

  private var loggingLevel = Level.INFO

  override fun setLogLevel(level: Level) {
    loggingLevel = level
  }

  override fun logLevel(): Level = loggingLevel

  override fun error(throwable: Throwable, msg: String) {
    val stringWriter = StringWriter()
    throwable.printStackTrace(PrintWriter(stringWriter))
    println("${"INFO".red()}: $msg:\n$stringWriter")
  }

  override fun error(msg: String) {
    println("${"ERROR".red()}: $msg")
  }

  override fun verbose(msg: String) {
    if (loggingLevel == VERBOSE)
      println("${"VERBOSE".green()}: $msg")
  }

  override fun info(msg: String) {
    println("${"INFO".green()}: $msg")
  }

  override fun warning(msg: String) {
    println("${"WARNING".yellow()}: $msg")
  }

  companion object {
    // colorful console output
    //  https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    private const val ANSI_RESET = "\u001B[0m"
    private const val ANSI_BLACK = "\u001B[30m"
    private const val ANSI_RED = "\u001B[31m"
    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_YELLOW = "\u001B[33m"
    private const val ANSI_BLUE = "\u001B[34m"
    private const val ANSI_PURPLE = "\u001B[35m"
    private const val ANSI_CYAN = "\u001B[36m"
    private const val ANSI_WHITE = "\u001B[37m"

    private fun String.red() = "$ANSI_RED$this$ANSI_RESET"
    private fun String.green() = "$ANSI_GREEN$this$ANSI_RESET"
    private fun String.yellow() = "$ANSI_YELLOW$this$ANSI_RESET"
  }
}