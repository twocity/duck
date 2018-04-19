package duck

interface Logger {
  fun setLogLevel(level: Level)

  fun logLevel(): Level

  fun error(throwable: Throwable, msg: String)

  fun error(msg: String)

  fun verbose(msg: String)

  fun info(msg: String)

  fun warning(msg: String)

  enum class Level {
    INFO, VERBOSE,
  }
}