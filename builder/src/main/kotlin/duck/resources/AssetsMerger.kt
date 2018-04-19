package duck.resources

import duck.Logger
import duck.clean
import java.nio.file.Path

class AssetsMerger(private val logger: Logger,
    private val assetsPaths: List<Path>) {

  fun merge(outputFolder: Path): Path {
    outputFolder.clean()
    val mergedAssets = mutableListOf<AssetItem>()

    for (asset in collectAssets(assetsPaths)) {
      val conflict = mergedAssets.find {
        it.name == asset.name && it.relativePath == it.relativePath
      }
      if (conflict != null) {
        val warning = buildString {
          append("Asset conflict:\n")
          append("(1): ${conflict.path}")
          append("(2): ${asset.path}")
          append("ignore ${asset.path}")
        }
        logger.warning(warning)
        continue
      }
      mergedAssets.add(asset)
    }
    copyAssets(mergedAssets.toList(), outputFolder)
    logger.info("Assets merged -> $outputFolder")
    return outputFolder
  }

  private fun copyAssets(assets: List<AssetItem>, rootFolder: Path) {
    assets.forEach {
      val target = rootFolder.resolve(it.relativePath)
      logger.verbose("Copy asset ${it.path} -> $target")
      it.path.toFile().copyTo(target.toFile())
    }
  }

  private fun collectAssets(sourceFolders: List<Path>): List<AssetItem> {
    val re = sourceFolders.fold(mutableListOf<AssetItem>()) { acc, folder ->
      val files = folder.toFile().walk().filter { it.isFile }
      val items = files.map { AssetItem.create(folder, it.toPath()) }
      acc.apply {
        addAll(items)
      }
    }
    return re.toList()
  }
}