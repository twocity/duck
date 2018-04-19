package duck.manifest

import com.android.manifmerger.ManifestProvider
import duck.aar.Aar
import java.io.File
import java.nio.file.Path

class ManifestDepsProviderImpl(private val aarDeps: List<Aar>) : ManifestDepsProvider {
  override fun manifests(): List<ManifestProvider> {
    return aarDeps.map { ManifestProviderImpl(it.androidManifest(), it.name()) }
  }

  private class ManifestProviderImpl(private val manifest: Path,
      private val name: String) : ManifestProvider {
    override fun getName(): String = name

    override fun getManifest(): File = manifest.toFile()
  }
}