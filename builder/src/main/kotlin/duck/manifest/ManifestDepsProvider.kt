package duck.manifest

import com.android.manifmerger.ManifestProvider

interface ManifestDepsProvider {
  fun manifests(): List<ManifestProvider>
}