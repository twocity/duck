package duck.aar

import java.nio.file.Path
import java.util.Optional

/**
 * Android AAR format
 * https://developer.android.com/studio/projects/android-library.html#aar-contents
 */
interface Aar {

  //// mandatory parts ////
  /**
   * AndroidManifest in aar
   */
  fun androidManifest(): Path

  /**
   * the resources
   */
  fun res(): Path

  /**
   * classes.jar
   */
  fun classesJar(): Path

  /**
   * R.txt
   */
  fun rTxt(): Path

  /**
   * A R.txt but starts with current library's package name as the first line, used to generate [SymbolTable]
   * This file is not part of aar
   */
  fun packageAwareRTxt(): Path

  //// optional parts ////
  /**
   * public.txt
   */
  fun publicTxt(): Optional<Path>

  /**
   * assets in aar
   */
  fun assets(): Optional<Path>

  /**
   * jni folder contains so binary files
   */
  fun jni(): Optional<Path>

  /**
   * java deps
   */
  fun libs(): Optional<Path>

  /**
   * lint.jar
   */
  fun lint(): Optional<Path> = throw IllegalStateException("Not supported yet")

  /**
   * proguard.txt
   */
  fun proguard(): Optional<Path> = throw IllegalStateException("Not supported yet")

  // name of the aar
  fun name(): String
}