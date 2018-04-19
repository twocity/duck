package duck

import okio.Buffer
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipEntry.DEFLATED
import java.util.zip.ZipFile

class JavaResMerger(private val logger: Logger, private val inputs: Collection<JavaRes>) {

  private val patchMatcher by lazy {
    val fileSystem = FileSystems.getDefault()
    DelegatePathMatcher(excludes.map { fileSystem.getPathMatcher("glob:$it") })
  }

  interface JavaRes {
    fun paths(): Iterable<Path>
    fun read(path: Path, sink: BufferedSink)
  }

  fun merge(outputFile: Path): Path {
    outputFile.parent.mkdirs()

    val pathToBuffer = mutableMapOf<Path, Buffer>()
    inputs.forEach { jar ->
      jar.paths()
          .filterNot {
            val absPath = if (!it.startsWith("/")) "/$it" else it.toString()
            val path = absPath.replace('/', File.separatorChar).toPath()
            patchMatcher.matches(path)
          }
          .forEach {
            readArchivePath(jar, it, pathToBuffer)
          }
    }

    val zipBuilder = ZipBuilder.newBuilder(outputFile)
    zipBuilder.use {
      pathToBuffer.forEach { path, buf ->
        logger.verbose("writer $path")
        zipBuilder.addEntry(path.toString(), buf, DEFLATED)
      }
    }
    require(outputFile.exists())
    logger.info("Java resources merged -> $outputFile")
    return outputFile
  }

  private fun readArchivePath(jar: JavaRes, path: Path, pathToBuffer: MutableMap<Path, Buffer>) {
    if (!pathToBuffer.containsKey(path)) {
      val buffer = Buffer()
      jar.read(path, buffer)
      pathToBuffer[path] = buffer
    } else {
      logger.warning("conflict $path")
    }
  }

  class JarJavaRes private constructor(private val jar: Path) : JavaRes {
    private val zipFile by lazy { ZipFile(jar.toFile()) }
    private val zipEntries by lazy { zipFile.entries().toList() }

    override fun paths(): Iterable<Path> {
      return zipEntries.filter { !it.isDirectory }.map { it.name.toPath() }
    }

    override fun read(path: Path, sink: BufferedSink) {
      val entry = zipEntries.find { it.name == path.toString() }
      require(entry != null) { "Not found $path" }
      zipFile.getInputStream(entry).use {
        val buffer = Okio.buffer(Okio.source(it)).buffer()
        sink.write(buffer, buffer.size())
      }
    }

    companion object {
      fun fromJar(jar: Path): JarJavaRes {
        require(jar.exists())
        return JarJavaRes(jar)
      }
    }
  }

  companion object {
    private val excludes = listOf(
        "/META-INF/LICENSE",
        "/META-INF/LICENSE.txt",
        "/META-INF/MANIFEST.MF",
        "/META-INF/NOTICE",
        "/META-INF/NOTICE.txt",
        "/META-INF/*.DSA",
        "/META-INF/*.EC",
        "/META-INF/*.SF",
        "/META-INF/*.RSA",
        "/META-INF/maven/**",
        "/NOTICE",
        "/NOTICE.txt",
        "/LICENSE.txt",
        "/LICENSE",

        // Exclude version control folders.
        "**/.svn/**",
        "**/CVS/**",
        "**/SCCS/**",

        // Exclude hidden and backup files.
        "**/.*/**",
        "**/.*",
        "**/*~",

        // Exclude index files
        "**/thumbs.db",
        "**/picasa.ini",

        // Exclude javadoc files
        "**/about.html",
        "**/package.html",
        "**/overview.html",

        // Exclude stuff for unknown reasons
        "**/_*",
        "**/_*/**",

        "**/*.java",
        // Exclude class file
        "**/*.class",
        // Exclude so file
        "**/*.so"
    )
  }

}