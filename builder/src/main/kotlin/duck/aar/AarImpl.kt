package duck.aar

import com.android.builder.symbols.SymbolIo
import duck.exists
import duck.toOptional
import java.nio.file.Path
import java.util.Optional

class AarImpl(private val name: String,
    private val dir: Path,
    private val assets: Optional<Path>,
    private val jni: Optional<Path>,
    private val libs: Optional<Path>,
    private val res: Path,
    private val manifest: Path,
    private val classesJar: Path,
    private val rTxt: Path,
    private val packageAwareRTxt: Path,
    /** R.txt **/
    private val publicTxt: Optional<Path>
    /** public.txt **/
) : Aar {

  override fun name(): String = name

  override fun assets(): Optional<Path> = assets

  override fun jni(): Optional<Path> = jni

  override fun libs(): Optional<Path> = libs

  override fun rTxt(): Path = rTxt

  override fun packageAwareRTxt(): Path = packageAwareRTxt

  override fun publicTxt(): Optional<Path> = publicTxt

  override fun androidManifest(): Path = manifest

  override fun classesJar(): Path = classesJar

  override fun res(): Path = res

  companion object {
    fun from(dir: Path, name: String): Aar {
      val manifest = dir.resolve("AndroidManifest.xml")
      require(manifest.exists()) { "Can't find `AndroidManifest.xml` in $dir" }

      val res = dir.resolve("res")
      require(res.exists()) { "Can't find `res` in $dir" }

      val classesJar = dir.resolve("classes.jar")
      require(classesJar.exists()) { "Can't find `classes.jar` in $dir" }

      val r = dir.resolve("R.txt")
      require(r.exists()) { "Can't find `R.txt` in $dir" }
      val packageAwareR = dir.resolve("package-aware-r.txt")
      SymbolIo.writeSymbolTableWithPackage(r, manifest, packageAwareR)
      return AarImpl(
          name = name,
          dir = dir,
          manifest = manifest,
          res = res,
          classesJar = classesJar,
          rTxt = r,
          packageAwareRTxt = packageAwareR,
          publicTxt = dir.resolve("public.txt").toOptional(),
          assets = dir.resolve("assets").toOptional(),
          jni = dir.resolve("jni").toOptional(),
          libs = dir.resolve("libs").toOptional()
      )
    }
  }
}