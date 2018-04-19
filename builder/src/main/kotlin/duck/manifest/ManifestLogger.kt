package duck.manifest

import com.android.utils.ILogger
import duck.Logger
import java.util.Locale

class ManifestLogger(private val logger: Logger) : ILogger {
  override fun warning(msgFormat: String, vararg args: Any?) {
    logger.warning(String.format(Locale.US, msgFormat, args))
  }

  override fun info(msgFormat: String, vararg args: Any?) {
    logger.info(String.format(Locale.US, msgFormat, args))
  }

  override fun error(t: Throwable?, msgFormat: String?, vararg args: Any?) {
    if (t == null) {
      logger.error(String.format(Locale.US, msgFormat ?: "", args))
    } else {
      logger.error(t, String.format(Locale.US, msgFormat ?: "", args))
    }
  }

  override fun verbose(msgFormat: String, vararg args: Any) {
    logger.verbose(String.format(Locale.US, msgFormat, args))
  }
}