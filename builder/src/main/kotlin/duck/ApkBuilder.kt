package duck

import com.android.apkzlib.zfile.ApkCreator
import com.android.apkzlib.zfile.ApkCreatorFactory.CreationData
import com.android.apkzlib.zfile.ApkZFileCreatorFactory
import com.android.apkzlib.zfile.NativeLibrariesPackagingMode
import com.android.apkzlib.zip.ZFileOptions
import com.android.apkzlib.zip.compress.BestAndDefaultDeflateExecutorCompressor
import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executors

class ApkBuilder(private val project: Project) {
  private val apkCreator by lazy { buildApkCreator() }
  private val compressExecutor = Executors.newCachedThreadPool()

  private lateinit var dexFile: File
  private lateinit var resZipFile: File
  private var javaRes: File? = null

  fun build() {
    apkCreator.use {
      it.writeFile(dexFile, "classes.dex")
      it.writeZip(resZipFile, null, null)
      javaRes?.let { apkCreator.writeZip(it, null, null) }
    }
    compressExecutor.shutdown()
  }

  fun dex(dex: Path) {
    dexFile = dex.toFile()
    require(dexFile.exists())
  }

  fun androidResources(res: Path) {
    resZipFile = res.toFile()
    require(resZipFile.exists())
  }

  fun javaResources(jar: Path) {
    javaRes = jar.toFile()
  }

  private fun buildApkCreator(): ApkCreator {
    val options = ZFileOptions().apply {
      coverEmptySpaceUsingExtraField = true
      autoSortFiles = true
      compressor = BestAndDefaultDeflateExecutorCompressor(compressExecutor, tracker, 1.0)
    }
    val factory = ApkZFileCreatorFactory(options)

    val signingConfig = project.androidConfig.signingConfig
    val certification = CertificateInfo.read(
        storeType = signingConfig.keyStoreType,
        storeFile = project.projectDir.resolve(signingConfig.keystore).toFile(),
        storePassword = signingConfig.storePassword,
        keyAlias = signingConfig.keyAlias,
        keyPassword = signingConfig.keyPassword
    )
    val creationData = CreationData(project.outputApk.toFile(),
        certification.key,
        certification.certificate,
        true, // false to generate unsigned apk
        signingConfig.v2,
        CREATE_BY,
        CREATE_BY,
        project.androidConfig.minSdkVersion,
        NativeLibrariesPackagingMode.COMPRESSED,
        { false })
    return factory.make(creationData)
  }

  companion object {
    private const val CREATE_BY = "duck-build"
  }

}