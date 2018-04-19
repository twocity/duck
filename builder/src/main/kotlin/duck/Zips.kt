package duck

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun File.extractTo(dir: Path) {
  val zipFile = ZipFile(this)
  ZipInputStream(FileInputStream(this)).use {
    var entry = it.nextEntry
    while (entry != null) {
      if (entry.isDirectory) {
        dir.resolve(entry.name).mkdirs()
      } else {
        val outFile = dir.resolve(entry.name)
        zipFile.getInputStream(entry).use {
          it.copyTo(FileOutputStream(outFile.toFile()))
        }
      }
      entry = it.nextEntry
    }
  }
}