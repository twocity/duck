package duck.resources

import java.nio.file.Path

data class MergedAndroidData(
    val mergedResources: Path,
    val mergedAssets: Path,
    val mergedManifest: Path
)
