package br.ufpe.liber.tasks

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateAssetsMetadataTask : DefaultTask() {

    @get:InputDirectory
    abstract val assetsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val assetsMetadataFile: RegularFileProperty

    init {
        assetsDirectory.convention(project.layout.buildDirectory.dir("resources/main/public"))
        assetsMetadataFile.convention(assetsDirectory.file("assets-metadata.json"))
    }

    @TaskAction
    fun generateAssetsMetadata() {
        val assetsParentDir = assetsDirectory.get().asFile
        val metafile = assetsMetadataFile.get().asFile

        val metadata: MutableList<JsonObject> = mutableListOf()
        val regex = "(?<filename>[A-Za-z/-]+).(?<hash>[A-Z0-9]{8}).(?<extension>[a-z]+)".toPattern()

        val digest = DigestUtils.getSha384Digest()
        val integrityGenerator = { file: File ->
            "sha384-${Base64.encodeBase64String(digest.digest(file.readBytes()))}"
        }

        val encodings = listOf("br", "gz", "zz")

        assetsParentDir.walk()
            .filter(File::isFile)
            .filter { !encodings.contains(it.extension) }
            .forEach { file ->
                val filename = file.absolutePath.removePrefix(assetsParentDir.absolutePath)
                val matcher = regex.matcher(filename)

                if (matcher.matches()) {
                    val integrity = integrityGenerator(file)
                    val mediaType = detectMediaType(file)

                    val basename = matcher.group("filename")
                    val hash = matcher.group("hash")
                    val extension = matcher.group("extension")

                    val source = "$basename.$extension"

                    metadata.add(
                        mapOf(
                            "basename" to basename.toJson(),
                            "source" to source.toJson(),
                            "filename" to filename.toJson(),
                            "hash" to hash.toJson(),
                            "integrity" to integrity.toJson(),
                            "extension" to extension.toJson(),
                            "mediaType" to mediaType.toString().toJson(),
                            "encodings" to JsonArray(findEncodings(file)),
                        ).toJson(),
                    )
                }
            }

        val prettyJson = Json {
            prettyPrint = true
        }

        metafile.writeText(prettyJson.encodeToString(metadata))
    }

    private val tika: TikaConfig = TikaConfig()

    @Suppress("detekt:ForbiddenComment")
    private fun detectMediaType(file: File): MediaType {
        val metadata = Metadata()
        // TODO: Check the return type for JavaScript (text/javascript vs application/javascript)
        // See https://issues.apache.org/jira/browse/TIKA-4119
        return tika.detector.detect(TikaInputStream.get(file.toPath(), metadata), metadata)
    }

    private fun findEncodings(file: File): List<JsonObject> = file
        .parentFile
        .walk()
        .maxDepth(1)
        .filter { it.name.startsWith(file.name) }
        .flatMap { encodedFile ->
            when (encodedFile.extension) {
                "br" -> listOf(
                    mapOf(
                        "http" to "br".toJson(),
                        "extension" to "br".toJson(),
                        "priority" to 0.toJson(),
                    ).toJson(),
                )

                "gz" -> listOf(
                    mapOf(
                        "http" to "gzip".toJson(),
                        "extension" to "gz".toJson(),
                        "priority" to 1.toJson(),
                    ).toJson(),
                )

                "zz" -> listOf(
                    mapOf(
                        "http" to "deflate".toJson(),
                        "extension" to "zz".toJson(),
                        "priority" to 2.toJson(),
                    ).toJson(),
                )

                else -> emptyList()
            }
        }.toList()

    private fun String.toJson(): JsonPrimitive = JsonPrimitive(this)
    private fun Number.toJson(): JsonPrimitive = JsonPrimitive(this)
    private fun Map<String, JsonElement>.toJson() = JsonObject(this)
}
