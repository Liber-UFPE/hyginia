package br.ufpe.liber.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.core.io.ResourceResolver
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.util.Optional

@MicronautTest
class BookTest(private val resourceResolver: ResourceResolver) : BehaviorSpec({
    given("Book loaded from JSON") {

        val book: Book =
            resourceResolver
                .getResourceAsStream("classpath:books/1635.utf8.json")
                .map { inputStream ->
                    val json = inputStream.bufferedReader().use(BufferedReader::readText)
                    Json.decodeFromString<Book>(json)
                }
                .orElse(
                    Book(
                        // Empty book
                        id = 0,
                        author = "",
                        title = "",
                        number = 0,
                        year = "",
                        period = "",
                        days = listOf(),
                    ),
                )

        `when`("decoded to model") {

            then("should have the correct author") { book.author shouldBe "José Hyginio" }
            then("should have the correct title") { book.title shouldBe "Dagelijkse Notulen" }
            then("should have the correct number") { book.number shouldBe 1 }
            then("should have the correct year") { book.year shouldBe "1635" }
            then("should have the correct period") { book.period shouldBe "27 de março a 31 de dezembro" }

            then("should have the correct first day") {
                val firstDay = book.days.first()
                firstDay.id shouldBe 1
                firstDay.day shouldBe "27 de março"
            }

            then("should have the correct last day") {
                val lastDay = book.days.last()
                lastDay.id shouldBe 39
                lastDay.day shouldBe "23 de maio"
            }
        }

        `when`(".hasNextDay") {
            then("first day should have next day") {
                book.hasNextDay(book.days.first().id) shouldBe true
            }

            then("some day in the middle should have next day") {
                val dayId: Long = (book.days.size / 2) + 1L
                book.hasNextDay(dayId) shouldBe true
            }

            then("last day should not have next day") {
                book.hasNextDay(book.days.last().id) shouldBe false
            }
        }

        `when`(".nextDay") {
            then("get day after first day") {
                book.nextDay(book.days.first().id) shouldBePresent {
                    it.id shouldBe 2
                    it.day shouldBe "29 de março"
                }
            }

            then("some day in the middle should have next day") {
                val dayId: Long = 9
                book.nextDay(dayId) shouldBePresent {
                    it.id shouldBe 10
                    it.day shouldBe "13 de abril"
                }
            }

            then("next day after last day should be empty") {
                book.nextDay(book.days.last().id) shouldBe Optional.empty()
            }
        }

        `when`(".hasPreviousDay") {
            then("last day should have previous day") {
                book.hasPreviousDay(book.days.last().id) shouldBe true
            }

            then("some day in the middle should have previous day") {
                val dayId: Long = (book.days.size / 2) + 1L
                book.hasPreviousDay(dayId) shouldBe true
            }

            then("first day should not have previous day") {
                book.hasPreviousDay(book.days.first().id) shouldBe false
            }
        }

        `when`(".previousDay") {
            then("get day before last day") {
                book.previousDay(book.days.last().id) shouldBePresent {
                    it.id shouldBe 38
                    it.day shouldBe "20 de maio"
                }
            }

            then("some day in the middle should have previous day") {
                val dayId: Long = 9
                book.previousDay(dayId) shouldBePresent {
                    it.id shouldBe 8
                    it.day shouldBe "11 de abril"
                }
            }

            then("previous day before first day should be empty") {
                book.previousDay(book.days.first().id) shouldBe Optional.empty()
            }
        }

        `when`(".day") {
            then("should return a day when id exists") {
                book.day(book.days.first().id) shouldBePresent {
                    it.id shouldBe 1
                    it.day shouldBe "27 de março"
                }
            }

            then("should return an empty optional when id does not exist") {
                // We know that there are no negative IDs
                book.day(-1L) shouldBe Optional.empty()
            }
        }
    }
})
