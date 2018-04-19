package duck

import java.nio.file.Path
import java.nio.file.Paths

class AndroidSdk private constructor(private val androidSdk: Path,
    private val buildToolVersion: String,
    private val compileSdk: Int) {

  val aapt = androidSdk.resolve("build-tools").resolve(
      buildToolVersion).resolve("aapt2")
  val frameworkJar = androidSdk.resolve("platforms").resolve(
      "android-$compileSdk").resolve("android.jar")!!
  val dx = androidSdk.resolve("build-tools").resolve(
      buildToolVersion).resolve("dx")

  init {
    require(aapt.exists(), { "$aapt does not exists" })
    require(dx.exists(), { "$dx does not exists" })
    require(frameworkJar.toFile().exists(),
        { "compile sdk version $compileSdk not exists" })
  }

  companion object {
    private val CANDIDATE_ANDROID_HOME = arrayOf("ANDROID_HOME", "ANDROID_SDK_HOME")

    fun create(buildToolVersion: String,
        compileSdk: Int): AndroidSdk {
      val androidHome: Path = findAndroidHome(CANDIDATE_ANDROID_HOME) ?: throw DuckException(
          "can't find ${CANDIDATE_ANDROID_HOME.joinToString(separator = ",")} in your environment")
      return AndroidSdk(androidHome, buildToolVersion, compileSdk)
    }

    private fun findAndroidHome(envs: Array<String>): Path? =
        envs.mapNotNull { System.getenv(it) }
            .map { it.toPath() }
            .firstOrNull { it.exists() }
  }
}