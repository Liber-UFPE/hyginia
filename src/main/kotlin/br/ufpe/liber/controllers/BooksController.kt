package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import br.ufpe.liber.services.BookRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.context.ServerRequestContext
import java.util.Optional

@Controller
@Produces(MediaType.TEXT_HTML)
class BooksController(private val bookRepository: BookRepository, private val templates: Templates) : KteController {
    @Get("/obras")
    fun index() = ok(templates.booksIndex(bookRepository.listAll()))

    @Get("/obra/{bookId}")
    fun show(bookId: Long): HttpResponse<KteWriteable> = bookRepository.get(bookId)
        .map { ok(templates.booksShow(it)) }
        .orElse(notFound(templates.notFound(currentRequestPath())))

    @Get("/obra/{bookId}/pagina/{dayId}")
    fun showDay(bookId: Long, dayId: Long, query: Optional<String>): HttpResponse<KteWriteable> = bookRepository
        .get(bookId)
        .flatMap { book ->
            book.day(dayId).map { day ->
                ok(templates.booksDay(book, day, query))
            }
        }
        .orElse(notFound(templates.notFound(currentRequestPath())))

    private fun currentRequestPath(): String = ServerRequestContext.currentRequest<Any>().map { it.path }.orElse("")
}
