@file:Suppress("ktlint")
package br.ufpe.liber.tasks

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Predicate

@CacheableTask
abstract class ParseBooksTask : DefaultTask() {
    private val bookIdGenerator = AtomicLong(1)
    private val dayIdGenerator = AtomicLong(1)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val booksFiles: Property<FileTree>

    init {
        booksFiles.convention(
            project.fileTree(project.layout.projectDirectory.dir("src/main/resources/books/")) {
                this.include("*.utf8.txt")
            },
        )

        this.outputs.files(
            project.layout.projectDirectory
                .files("src/main/resources/books/")
                .filter { it.name.endsWith(".json") },
        )
    }

    @Suppress("ktlint")
    private fun parse(resource: File): JsonObject {
        val locale = Locale.forLanguageTag("pt-BR")
        val dayFormat = SimpleDateFormat("d 'de' MMMM", locale)
        val isDay: Predicate<String> = Predicate<String> { line ->
            try {
                dayFormat.parse(line)
                true
            } catch (ex: ParseException) {
                false
            }
        }

        var author: String? = null
        var title: String? = null
        var number: Int? = null
        var year: String? = null
        var period: String? = null
        val days: MutableList<JsonObject> = mutableListOf()

        var currentDay: String? = null
        var pageContents: StringBuilder = StringBuilder()

        resource.forEachLine { line ->
            when {
                line.startsWith("author: ") -> author = line.substringAfter("author: ")
                line.startsWith("title: ") -> title = line.substringAfter("title: ")
                line.startsWith("number: ") -> number = line.substringAfter("number: ").trim().toInt()
                line.startsWith("year: ") -> year = line.substringAfter("year: ")
                line.startsWith("period: ") -> period = line.substringAfter("period: ")
                isDay.test(line) -> {
                    if (currentDay != null) {
                        days.add(
                            JsonObject(
                                mapOf(
                                    "id" to JsonPrimitive(dayIdGenerator.getAndIncrement()),
                                    "day" to JsonPrimitive(currentDay!!), // skipcq: KT-E1010
                                    "contents" to JsonPrimitive(pageContents.toString()),
                                ),
                            ),
                        )
                    }
                    currentDay = line.trim()
                    pageContents = StringBuilder()
                }

                else -> pageContents.append(line).append("\n")
            }
        }

        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(bookIdGenerator.getAndIncrement()),
                "author" to JsonPrimitive(author!!), // skipcq: KT-E1010
                "title" to JsonPrimitive(title!!), // skipcq: KT-E1010
                "number" to JsonPrimitive(number!!), // skipcq: KT-E1010
                "year" to JsonPrimitive(year!!), // skipcq: KT-E1010
                "period" to JsonPrimitive(period!!), // skipcq: KT-E1010
                "days" to JsonArray(days), // skipcq: KT-E1010
            ),
        )
    }

    @TaskAction
    fun parseBooks() {
        val prettyJson = Json {
            prettyPrint = true
        }

        this.booksFiles
            .get()
            .sorted()
            .forEach { file ->
                logger.info("\t Parse book file $file")

                val book = parse(file)

                // No need to run this for each build, so the files can be generated
                // right into the resource folder.
                val jsonFile = "${file.absolutePath.substringBeforeLast(".")}.json"
                File(jsonFile).writeText(prettyJson.encodeToString(book))

                logger.info("\t JSON file $jsonFile generated")
            }
    }
}
