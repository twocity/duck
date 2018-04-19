package buck.downloader

import java.io.File
import java.nio.file.Paths

/**
 * A simple tool to download dependencies manually
 */
fun main(args: Array<String>) {
  val outputDirectory = Paths.get("artifact-downloader/build/local-repository")
      .toAbsolutePath()
  val downloader = MavenDownloader(RepositoryFactory.defaultRemoteRepositories,
      outputDirectory)

  downloader.download("com.android.support:appcompat-v7:aar:27.1.1", transitive = true)
      .forEach {
        println("artifact: ${it.file.name}")
        val targetName = "${it.groupId}-${it.artifactId}-${it.version}.${it.file.extension}"
        val target = File("android-project-example/libs/android", targetName)
        it.file.copyTo(target, overwrite = true)
      }
}