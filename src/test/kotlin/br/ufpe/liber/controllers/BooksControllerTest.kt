package br.ufpe.liber.controllers

import br.ufpe.liber.asString
import br.ufpe.liber.assets.AssetsResolver
import br.ufpe.liber.get
import br.ufpe.liber.services.BookRepository
import br.ufpe.liber.views.Markdown
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.kotlin.context.getBean
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class BooksControllerTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({
    val client = context
        .createBean(
            HttpClient::class.java,
            server.url,
            DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
        )
        .toBlocking()

    val bookRepository = context.getBean<BookRepository>()

    beforeSpec {
        context.getBean<BookRepository>()
        context.getBean<AssetsResolver>()
    }

    given("#index") {
        `when`("accessing the list of books") {
            then("show all the available books") {
                val response = client.get("/obras")
                response.status() shouldBe HttpStatus.OK

                val body = response.body()
                bookRepository.listAll().forEach { book ->
                    body shouldContain book.title
                }
            }
        }
    }

    given("#show") {
        `when`("accessing books details") {
            then("shows the first day") {
                val book = bookRepository.listAll().random()

                val response = client.get("/obra/${book.id}")
                response.status() shouldBe HttpStatus.OK

                val firstDayHtml = Markdown.toHtml(book.firstDay().contents).asString()
                response.body() shouldContain firstDayHtml
            }
        }

        `when`("trying to access a book that does not exist") {
            then("returns HTTP 404") {
                val nonExistingBookId = bookRepository.listAll().last().id + 100
                client.get("/obra/$nonExistingBookId").status() shouldBe HttpStatus.NOT_FOUND
            }
        }
    }

    given("#showDay") {
        `when`("accessing a day") {
            val book = bookRepository.listAll().random()
            val day = book.days.random()

            val response = client.get("/obra/${book.id}/pagina/${day.id}")

            then("returns HTTP 200") {
                response.status() shouldBe HttpStatus.OK
            }

            then("shows day's content") {
                val dayHtml = Markdown.toHtml(day.contents).asString()
                response.body() shouldContain dayHtml
            }
        }

        `when`("trying to access a day that does not exist") {
            then("returns HTTP 404") {
                val book = bookRepository.listAll().random()
                val day = book.days.last()
                val nonExistingDayId = day.id + 100

                client.get("/obra/${book.id}/pagina/$nonExistingDayId").status() shouldBe HttpStatus.NOT_FOUND
            }
        }
    }
})
