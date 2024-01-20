package br.ufpe.liber.services

import br.ufpe.liber.model.Book
import io.micronaut.context.annotation.Bean
import io.micronaut.core.async.publisher.AsyncSingleResultPublisher
import io.micronaut.core.io.ResourceResolver
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.scheduling.TaskExecutors
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import org.reactivestreams.Publisher
import java.io.BufferedReader
import java.util.*
import java.util.concurrent.ExecutorService

@Singleton
class BookRepository(private val resourceResolver: ResourceResolver) {

    private val books: SortedMap<Long, Book>

    init {
        books = TreeMap()

        @Suppress("detekt:MagicNumber")
        listOf(1635, 1636, 1637, 1638, 1639, 1640, 1641, 1644).forEach { year ->
            resourceResolver.getResourceAsStream("classpath:books/$year.utf8.json").ifPresent { inputStream ->
                val json = inputStream.bufferedReader().use(BufferedReader::readText)
                val book = Json.decodeFromString<Book>(json)
                books[book.id] = book
            }
        }
    }

    fun listAll(): List<Book> = books.values.toList()
    fun get(id: Long): Optional<Book> = Optional.ofNullable(books[id])
}

@Bean
class BookRepositoryHealthIndicator(
    private val bookRepository: BookRepository,
    @Named(TaskExecutors.BLOCKING) private val executorService: ExecutorService,
) : HealthIndicator {
    companion object {
        private val UP = HealthStatus(HealthStatus.NAME_UP, "Books Repository is operational", true, null)
        private val DOWN = HealthStatus(HealthStatus.NAME_DOWN, "Books Repository is NOT operational", false, null)
    }

    override fun getResult(): Publisher<HealthResult> = AsyncSingleResultPublisher(executorService, ::getHealthResult)

    private fun getHealthResult(): HealthResult {
        val builder = HealthResult.builder("repository")
        return if (bookRepository.listAll().isNotEmpty()) {
            builder.status(UP).build()
        } else {
            builder.status(DOWN).build()
        }
    }
}
