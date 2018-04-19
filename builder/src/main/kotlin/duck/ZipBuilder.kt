package duck

import okio.Buffer
import java.io.Closeable
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipBuilder private constructor(path: Path) : Closeable {
  private val zipStream = ZipOutputStream(FileOutputStream(path.toFile()))

  fun addEntry(name: String, buffer: Buffer, method: Int): ZipBuilder {
    require(!name.startsWith("/")) { "can't write absolute path $name" }
    val entry = ZipEntry(name)
    entry.method = method
    entry.size = buffer.size()
    val crC32 = CRC32()
    val bytes = buffer.readByteArray()
    crC32.update(bytes)
    entry.crc = crC32.value

    // write ZipEntry
    zipStream.putNextEntry(entry)
    zipStream.write(bytes)
    zipStream.closeEntry()
    return this
  }

  override fun close() {
    zipStream.close()
  }

  companion object {
    fun newBuilder(path: Path): ZipBuilder = ZipBuilder(path)
  }
}