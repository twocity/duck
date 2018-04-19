package duck.resources

import java.nio.file.Path

class ResourcesOutput(
    val androidManifest: Path,
    val resourcesApk: Path,
    val primaryRTxt: Path,
    val sourcesDirectory: Path
)
