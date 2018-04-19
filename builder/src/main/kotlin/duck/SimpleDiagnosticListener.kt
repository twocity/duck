package duck

import javax.tools.Diagnostic
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.WARNING
import javax.tools.DiagnosticListener
import javax.tools.JavaFileObject

class SimpleDiagnosticListener(private val logger: Logger) : DiagnosticListener<JavaFileObject> {
  override fun report(diagnostic: Diagnostic<out JavaFileObject>) {
    val message = DiagnosticPrettyPrinter.format(diagnostic)
    when (diagnostic.kind) {
      ERROR -> logger.error(message)
      WARNING -> logger.warning(message)
      else -> logger.info(message)
    }
  }
}