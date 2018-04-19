package duck

import duck.aar.Aar
import duck.aar.AarExtractor
import java.nio.file.Path

class DependencyManager(private val logger: Logger, private val fileFinder: FileFinder,
    private val aarExtractor: AarExtractor,
    private val project: Project) {

  lateinit var aarDeps: List<Aar>
  lateinit var jarDeps: List<Path>

  fun configure() {
    aarDeps = fileFinder.find(project.aarDeps)
        .map { aarExtractor.extract(it.toPath()) }
        .toList()

    val declaredJars = fileFinder.find(project.jarDeps)
        .map { it.toPath() }.toMutableList()
    val jarsFromAar = aarDeps.fold(mutableListOf<Path>()) { acc, aar ->
      acc.apply {
        add(aar.classesJar())
      }
    }
    jarDeps = declaredJars.apply { addAll(jarsFromAar) }
        .toList()
    logger.verbose("aars: ${aarDeps.joinToString(separator = ",")}")
    logger.verbose("jars: ${jarDeps.joinToString(separator = ",")}")
  }

  fun classPaths(): List<Path> {
    return jarDeps.fold(aarDeps.map { it.classesJar() }) { acc, path ->
      acc.toMutableList().apply { add(path) }.toList()
    }
  }
}