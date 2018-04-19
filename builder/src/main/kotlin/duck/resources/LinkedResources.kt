package duck.resources

import duck.resources.ResourcesLinker.AaptLinkOptions
import java.nio.file.Path

data class LinkedResources(val resourcesApk: Path,
    val symbolsOutput: Path,
    /** R.txt **/
    val javaSourceOutput: Path,
    /** generated R.java folder **/
    val aaptLinkOptions: AaptLinkOptions
)
