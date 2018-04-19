package duck

import duck.aar.AarExtractorImpl
import duck.manifest.ManifestDepsProviderImpl
import java.nio.file.FileSystems

class ProjectContext(val project: Project) {
  private val fileSystem = FileSystems.getDefault()!!
  val fileFinder = FileFinderImpl(fileSystem, project.projectDir)
  val logger = LoggerImpl()
  private val aarExtractor = AarExtractorImpl(logger, project.projectDir.resolve(".aars"))
  val dependencyManager by lazy { DependencyManager(logger, fileFinder, aarExtractor, project) }
  val sourceCodeManager by lazy {
    SourceCodeManager(fileFinder, project.androidConfig.srcDirsPattern)
  }

  /**
   * the android sdk home
   */
  val androidSdk = AndroidSdk.create(project.androidConfig.buildToolVersion,
      project.androidConfig.compileSdkVersion)

  val manifestDepsProvider by lazy { ManifestDepsProviderImpl(dependencyManager.aarDeps) }
}