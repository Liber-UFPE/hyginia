package br.ufpe.liber.services

import br.ufpe.liber.views.Markdown
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import java.util.Optional

@MicronautTest
class BookRepositoryTest(private val bookRepository: BookRepository) : BehaviorSpec({
    given("BookRepository") {
        `when`(".listAll") {
            then("should return all the books") {
                val books = bookRepository.listAll()
                books.size shouldBe 8
                books shouldBeSortedWith { book1, book2 -> book1.number - book2.number }
            }
        }

        `when`(".get") {
            then("should return book when id exists") {
                bookRepository.get(1) shouldBePresent {
                    it.id shouldBe 1
                    it.number shouldBe 1
                }
            }

            then("should return an empty optional when id does not exist") {
                bookRepository.get(1000) shouldBe Optional.empty()
            }
        }

        `when`(".hasBooks") {
            then("should return true when there are books") {
                bookRepository.hasBooks() shouldBe true
            }
        }
    }

    given("All Books") {
        `when`("rendering content as markdown") {
            then("all the pages succeed") {
                // Only to check that all contents are markdown compatible
                bookRepository
                    .listAll()
                    .flatMap { it.days }
                    .forEach { Markdown.toHtml(it.contents) }
            }
        }
    }
})
