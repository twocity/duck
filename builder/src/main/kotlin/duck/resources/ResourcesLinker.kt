package duck.resources

import duck.DuckException
import duck.Logger
import duck.exists
import duck.run
import java.nio.file.Path

class ResourcesLinker(private val logger: Logger,
    private val options: AaptLinkOptions) {

  fun link(resources: CompiledResources) {
    val commands = mutableListOf(options.aapt.toString())
    val outputResourceApk = options.resourcesApkOutput
    val outputSymbols = options.symbolsOutput
    with(commands) {
      add("link")
      add("-I")
      add(options.androidJar.toString())
      add("-A")
      add(resources.assets.toString())
      add("--manifest")
      add(resources.manifest.toString())
      add("-o")
      add(outputResourceApk.toString())
      add("-0")
      add("apk")
      add("--java")
      add(options.javaSourceDir.toString())
      add("--output-text-symbols")
      add(outputSymbols.toString())
      add("--emit-ids")
      add(outputSymbols.parent.resolve("ids.txt").toString())
      add("--no-version-vectors")
      if (options.packageName != null) {
        add("--custom-package")
        add(options.packageName.toString())
      }
      add("--auto-add-overlay")
      resources.resources.forEach {
        commands.add("-R")
        commands.add(it.toString())
      }
    }
    logger.verbose(
        commands.subList(1, commands.size).joinToString(separator = "\n"))
    if (!commands.run()) throw DuckException("Failed to run aapt link")
    if (!outputResourceApk.exists()) {
      throw DuckException("Can't find $outputResourceApk after aapt link")
    }
    if (!outputSymbols.exists()) {
      throw DuckException("Can't find $outputSymbols after aapt link")
    }
    logger.info("resources linked")
  }

  class AaptLinkOptions(val aapt: Path) {
    lateinit var androidJar: Path
    /**
     * Path to generate *.ap_
     */
    lateinit var resourcesApkOutput: Path
    /**
     * Directory to generate R.java
     */
    lateinit var javaSourceDir: Path
    /**
     * Path to generate R.txt
     */
    lateinit var symbolsOutput: Path

    var packageName: String? = null

    companion object {
      fun build(aapt: Path, init: AaptLinkOptions.() -> Unit): AaptLinkOptions {
        return AaptLinkOptions(aapt).apply { init() }
      }
    }
  }

}

