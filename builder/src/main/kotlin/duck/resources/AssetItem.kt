package duck.resources

import java.nio.file.Path

/**
 * Data class represents an android asset file
 *
 * [path] the path of current asset file
 * Since Android `assets` can contains folders, the [relativePath] is the path of asset file to
 * the root of assets folder
 * [name] asset file name
 *
 * For example, given two assets settings:
 *
 * + projectDir/main/assets/folder/test.txt
 * + projectDir/main/assets/test.txt
 *
 * which will produce two [AssetItem]:
 * AssetItem("projectDir/main/assets/folder/test.txt", "folder/test.txt", "test.txt")
 * AssetItem("projectDir/main/assets/test.txt", "test.txt", "test.txt")
 *
 * TODO handle gzipped files
 * https://android.googlesource.com/platform/tools/base/+/master/sdk-common/src/main/java/com/android/ide/common/res2/AssetItem.java
 */
data class AssetItem(val path: Path, val relativePath: Path,
    val name: String) {

  companion object {
    fun create(assetsFolder: Path, asset: Path): AssetItem {
      return AssetItem(asset, assetsFolder.relativize(asset), asset.fileName.toString())
    }
  }
}