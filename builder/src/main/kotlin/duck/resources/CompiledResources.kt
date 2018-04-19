package duck.resources

import java.nio.file.Path

data class CompiledResources(val resources: List<Path>,
    val assets: Path,
    val manifest: Path)
