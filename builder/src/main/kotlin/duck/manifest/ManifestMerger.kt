package duck.manifest

import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.ManifestMerger2.Invoker
import com.android.manifmerger.ManifestMerger2.MergeType
import com.android.manifmerger.ManifestProvider
import com.android.manifmerger.ManifestSystemProperty
import com.android.manifmerger.ManifestSystemProperty.MIN_SDK_VERSION
import com.android.manifmerger.ManifestSystemProperty.PACKAGE
import com.android.manifmerger.ManifestSystemProperty.TARGET_SDK_VERSION
import com.android.manifmerger.ManifestSystemProperty.VERSION_CODE
import com.android.manifmerger.ManifestSystemProperty.VERSION_NAME
import com.android.manifmerger.MergingReport
import com.android.manifmerger.MergingReport.Result.ERROR
import com.android.manifmerger.MergingReport.Result.SUCCESS
import com.android.manifmerger.MergingReport.Result.WARNING
import com.android.utils.ILogger
import duck.DuckException
import duck.Logger
import duck.saveAsFile
import java.io.File
import java.nio.file.Path

class ManifestMerger private constructor(
    private val logger: ILogger,
    private val manifestInvoker: Invoker<*>) {

  fun merge(outputManifest: Path): Path {
    val report = manifestInvoker.merge()
    when (report.result!!) {
      SUCCESS -> {
        val xmlDocument = report.getMergedDocument(
            MergingReport.MergedManifestKind.MERGED)
        val annotatedDocument = report.getMergedDocument(
            MergingReport.MergedManifestKind.BLAME)
        annotatedDocument?.let { logger.verbose(it) }
        xmlDocument.saveAsFile(outputManifest)
        logger.info("Manifest merged -> $outputManifest")
      }
      WARNING -> report.log(logger)
      ERROR -> {
        report.log(logger)
        throw DuckException(report.reportString)
      }
    }
    return outputManifest
  }

  class Builder {
    lateinit var mainManifest: File
    var deps: List<ManifestProvider> = listOf()
    var packageName: String? = null
    var versionName: String? = null
    var versionCode = -1
    var minSdkVersion = -1
    var targetSdkVersion = -1
  }

  companion object {
    fun create(logger: Logger, workingDir: Path, init: Builder.() -> Unit): ManifestMerger {
      val builder = Builder()
      val iLogger = ManifestLogger(logger)
      builder.init()
      val merger = ManifestMerger2.newMerger(
          builder.mainManifest,
          iLogger,
          MergeType.APPLICATION
      )
      with(merger) {
        withFeatures(Invoker.Feature.REMOVE_TOOLS_DECLARATIONS)
        addManifestProviders(builder.deps)
        setMergeReportFile(workingDir.resolve("manifest/manifest.log").toFile())
        // override if present
        if (!builder.packageName.isNullOrBlank()) {
          setOverride(PACKAGE, builder.packageName!!)
        }
        if (!builder.versionName.isNullOrBlank()) {
          setOverride(VERSION_NAME, builder.versionName!!)
        }
        if (builder.versionCode > 0) {
          setOverride(VERSION_CODE, builder.versionCode.toString())
        }
        if (builder.minSdkVersion > 0) {
          setOverride(MIN_SDK_VERSION, builder.minSdkVersion.toString())
        }
        if (builder.targetSdkVersion > 0) {
          setOverride(TARGET_SDK_VERSION, builder.targetSdkVersion.toString())
        }
      }
      return ManifestMerger(iLogger, merger)
    }
  }
}