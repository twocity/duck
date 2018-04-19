package duck

import okio.Okio
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Optional

fun Path.exists(): Boolean = Files.exists(this)

fun Path.toOptional(): Optional<Path> =
    if (Files.exists(this)) Optional.of(this)
    else Optional.empty()

fun Path.mkdirs(): Path = Files.createDirectories(this)!!

fun Path.deleteRecursively(): Boolean = toFile().deleteRecursively()
    .apply { if (!this) throw DuckException("$this deleteRecursively failed") }


fun Path.clean(): Path {
  apply {
    deleteRecursively()
    mkdirs()
  }
  return this
}

fun File.isEmpty(): Boolean {
  Okio.buffer(Okio.source(this)).use {
    return it.exhausted()
  }
}

fun String.toPath(): Path = Paths.get(this)

/**
 * Write given string  into file [out]
 */
fun String.saveAsFile(out: Path) {
  val content = this
  out.parent.mkdirs()
  Okio.buffer(Okio.sink(out)).use {
    it.writeUtf8(content)
  }
}
