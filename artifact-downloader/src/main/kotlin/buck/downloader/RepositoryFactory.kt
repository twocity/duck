package buck.downloader

import org.eclipse.aether.repository.RemoteRepository

object RepositoryFactory {
  val defaultRemoteRepositories by lazy {
    val google = RemoteRepository.Builder("google", "default",
        "https://maven.google.com/")
        .build()
    val mavenCentral = RemoteRepository.Builder(
        "central", "default", "https://repo1.maven.org/maven2/").build()
    listOf(google, mavenCentral)
  }
}