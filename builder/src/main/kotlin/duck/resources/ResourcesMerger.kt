package duck.resources

import com.android.ide.common.res2.MergedResourceWriter
import com.android.ide.common.res2.NoOpResourcePreprocessor
import com.android.ide.common.res2.ResourceMerger
import com.android.ide.common.res2.ResourceSet
import com.android.repository.Revision
import duck.Logger
import duck.clean
import duck.manifest.ManifestLogger
import java.nio.file.Path

class ResourcesMerger(private val logger: Logger,
    private val resourcesPaths: List<Path>,
    private val workingDir: Path,
    private val minSdkVersion: Int) {

  fun merge(outputPath: Path): Path {
    outputPath.clean()
    logger.verbose("Prepare merging ${resourcesPaths.joinToString(separator = ":")} to $outputPath")
    val merger = ResourceMerger(minSdkVersion)
    resourcesPaths.forEach {
      val set = ResourceSet(it.toString(), null, null, true)
      set.setDontNormalizeQualifiers(true)
      set.addSource(it.toFile())
      set.loadFromFiles(ManifestLogger(logger))
      merger.addDataSet(set)
    }
    val writer = MergedResourceWriter.createWriterWithoutPngCruncher(
        outputPath.toFile(),
        null,
        null,
        NoOpResourcePreprocessor.INSTANCE,
        workingDir.toFile()
    )
    merger.mergeData(writer, /* cleanUp */ false)
    logger.info("Resource merged -> $outputPath")
    return outputPath
  }
}