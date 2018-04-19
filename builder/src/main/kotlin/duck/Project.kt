package duck

import kotlinx.serialization.json.JSON
import okio.Okio
import java.io.File
import java.nio.file.Path

data class Project(
    private val config: ProjectConfig,
    val projectDir: Path) {

  val androidConfig = config.android
  val jarDeps = config.jars
  val aarDeps = config.aars
  val annotationProcessorOptions = config.annotationProcessorOptions

  val manifest: Path = projectDir.resolve(androidConfig.manifest)
  val buildDir by lazy { projectDir.resolve(BUILD_DIR_NAME)!! }
  val outputApk by lazy { buildDir.resolve("${androidConfig.projectName}.apk")!! }

  init {
    require(manifest.toFile().exists(), { "$manifest not exists" })
  }

  companion object {
    const val BUILD_DIR_NAME = "build"
    private const val GENERATED_SOURCE = "$BUILD_DIR_NAME/source"
    const val APT_DIR = "$GENERATED_SOURCE/apt"
    const val JAVAC_OUTPUT = "classes"
    const val RES_OUTPUT = "res"

    fun from(configFile: Path): Project {
      val json = Okio.buffer(Okio.source(configFile)).readUtf8()
      val config: ProjectConfig = JSON.parse(json)
      // TODO validate config values
      return Project(config, configFile.normalize().toAbsolutePath().parent)
    }
  }
}
