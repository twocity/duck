package buck.downloader

import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.util.artifact.JavaScopes
import java.nio.file.Path
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.util.filter.DependencyFilterUtils


class MavenDownloader(private val remoteRepositories: List<RemoteRepository>,
    private val outputDirectory: Path) {

  fun download(artifactCoords: String, transitive: Boolean = false): List<Artifact> {
    val artifact = DefaultArtifact(artifactCoords)
    val connector = MavenConnector(outputDirectory.toString())
    val system = connector.newRepositorySystem()
    val session = connector.newRepositorySystemSession(system)
    return if (!transitive) {
      listOf(downloadArtifact(artifact, session, system))
    } else {
      downloadArtifactTransitively(artifact, session, system)
    }
  }

  private fun downloadArtifact(artifact: Artifact, session: RepositorySystemSession,
      system: RepositorySystem): Artifact {
    val artifactRequest = ArtifactRequest().also {
      it.artifact = artifact
      it.repositories = remoteRepositories
    }
    val artifactResult = system.resolveArtifact(session, artifactRequest)
    return artifactResult.artifact
  }

  private fun downloadArtifactTransitively(artifact: Artifact, session: RepositorySystemSession,
      system: RepositorySystem): List<Artifact> {
    println("resolve transitive dependencies")

    val collectRequest = CollectRequest(Dependency(artifact, JavaScopes.COMPILE),
        remoteRepositories)
    val classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE)

    val dependencyRequest = DependencyRequest(collectRequest, classpathFilter)
    val result = system.resolveDependencies(session, dependencyRequest)
    return result.artifactResults.map { it.artifact }
  }
}