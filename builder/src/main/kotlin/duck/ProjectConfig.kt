package duck

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.KeyStore

@Serializable
data class SigningConfig(
    @SerialName("key_store_file")
    val keystore: String,
    @SerialName("key_store_password")
    val storePassword: String,
    @SerialName("key_alias")
    val keyAlias: String,
    @SerialName("key_password")
    val keyPassword: String,
    @Optional
    @SerialName("store_type")
    val keyStoreType: String = KeyStore.getDefaultType(),
    @Optional
    val v2: Boolean = true
)

@Serializable
data class AndroidConfig(
    @SerialName("name")
    val projectName: String,
    @SerialName("compile_sdk_version")
    val compileSdkVersion: Int,
    @SerialName("min_sdk_version")
    val minSdkVersion: Int,
    @SerialName("target_sdk_version")
    val targetSdkVersion: Int,
    @SerialName("build_tool_version")
    val buildToolVersion: String,
    @SerialName("application_id")
    val applicationId: String,
    @SerialName("version_code")
    val versionCode: Int,
    @SerialName("version_name")
    val versionName: String,
    val manifest: String,
    @SerialName("src")
    val srcDirsPattern: List<String>,
    @SerialName("res_dirs")
    val resDirs: List<String>,
    @SerialName("assets_dirs")
    @Optional
    val assetsFolders: List<String> = listOf(),
    @SerialName("signing")
    val signingConfig: SigningConfig)

@Serializable
data class AnnotationProcessorOptions(
    @SerialName("processor_classnames")
    val processorClassNames: List<String>,
    @SerialName("jar_deps")
    @Optional
    val jars: List<String> = listOf(),
    @Optional
    val arguments: Map<String, String> = mapOf()
) {
  companion object {
    val EMPTY = AnnotationProcessorOptions(listOf(), listOf())
  }
}

@Serializable
data class ProjectConfig(val android: AndroidConfig,
    @SerialName("jar_deps")
    @Optional
    val jars: List<String> = listOf(),
    @SerialName("aar_deps")
    @Optional
    val aars: List<String> = listOf(),
    @SerialName("annotation_processing")
    @Optional
    val annotationProcessorOptions: AnnotationProcessorOptions = AnnotationProcessorOptions.EMPTY)