package br.ufpe.liber.search

import br.ufpe.liber.model.Book
import br.ufpe.liber.model.Day
import br.ufpe.liber.pagination.Pagination
import br.ufpe.liber.services.BookRepository
import gg.jte.Content
import io.micronaut.context.annotation.Bean
import io.micronaut.core.async.publisher.AsyncSingleResultPublisher
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollectorManager
import org.reactivestreams.Publisher
import java.util.concurrent.ExecutorService
import kotlin.math.max
import kotlin.math.min

@Singleton
class BooksSearch(
    private val indexSearcher: IndexSearcher,
    private val analyzer: Analyzer,
    private val textHighlighter: TextHighlighter,
    private val bookRepository: BookRepository,
) {
    companion object {
        const val MAX_HITS_THRESHOLD: Int = 1000
    }

    fun search(keywords: String, page: Int = 0): SearchResults {
        val queryParser = QueryParser(DayMetadata.CONTENTS, analyzer)
        val query = queryParser.parse(keywords)
        val highlighter = textHighlighter.highlighter(query)

        val storedFields = indexSearcher.storedFields()
        val indexReader = indexSearcher.indexReader
        val termVectors = indexReader.termVectors()

        val collector = TopScoreDocCollectorManager(MAX_HITS_THRESHOLD, MAX_HITS_THRESHOLD + 1)
        val topDocs = indexSearcher.search(query, collector)

        if (topDocs.totalHits.value == 0L) return SearchResults.empty()

        val pagingStart: Int = max(page, 0) * Pagination.DEFAULT_PER_PAGE
        val pagingEnd: Int = min(pagingStart + Pagination.DEFAULT_PER_PAGE, topDocs.totalHits.value.toInt())

        val searchResults = topDocs.scoreDocs.slice(pagingStart..<pagingEnd).map { scoreDoc: ScoreDoc ->
            val document = storedFields.document(scoreDoc.doc)
            val dayContents = document[DayMetadata.CONTENTS]

            val fields = termVectors[scoreDoc.doc]
            val highlightedContent = textHighlighter.highlightContent(highlighter, dayContents, fields)

            bookRepository.get(document[BookMetadata.ID].toLong())
                .flatMap { book ->
                    book.day(document[DayMetadata.ID].toLong())
                        .map { day -> Pair(book, day) }
                }
                .map { (book, day) -> SearchResult(book, day, highlightedContent) }
                .orElse(SearchResult(document, highlightedContent))
        }

        return SearchResults(topDocs.totalHits.value.toInt(), searchResults, page + 1)
    }
}

@Serdeable
data class SearchResult(
    val book: Book,
    val day: Day,
    val highlightedContent: Content,
) {
    constructor(doc: Document, highlightedContent: Content) : this(
        Book(
            id = doc[BookMetadata.ID].toLong(),
            author = doc[BookMetadata.AUTHOR],
            title = doc[BookMetadata.TITLE],
            number = doc[BookMetadata.NUMBER].toInt(),
            year = doc[BookMetadata.YEAR],
            period = doc[BookMetadata.PERIOD],
            days = listOf(),
        ),
        Day(
            id = doc[DayMetadata.ID].toLong(),
            day = doc[DayMetadata.DAY],
            contents = doc[DayMetadata.CONTENTS],
        ),
        highlightedContent,
    )
}

@Serdeable
data class SearchResults(
    val hits: Int = 0,
    val items: List<SearchResult> = listOf(),
    val currentPage: Int = Pagination.DEFAULT_FIRST_PAGE,
) : Collection<SearchResult> by items {
    companion object {
        fun empty() = SearchResults()
    }

    val pagination = Pagination(hits, currentPage)
}

@Bean
class SearchHealthIndicator(
    private val search: BooksSearch,
    @Named(TaskExecutors.BLOCKING) private val executorService: ExecutorService,
) : HealthIndicator {
    companion object {
        private const val DEFAULT_TEST_SEARCH = "recife"
        private val UP = HealthStatus(HealthStatus.NAME_UP, "Search is operational", true, null)
        private val DOWN = HealthStatus(HealthStatus.NAME_DOWN, "Search is NOT operational", false, null)
    }

    override fun getResult(): Publisher<HealthResult> = AsyncSingleResultPublisher(executorService, ::getHealthResult)

    @Suppress("detekt:TooGenericExceptionCaught")
    private fun getHealthResult(): HealthResult {
        val builder = HealthResult.builder("search")
        return try {
            search.search(DEFAULT_TEST_SEARCH)
            builder.status(UP).build()
        } catch (ex: Exception) {
            builder.exception(ex).status(DOWN).build()
        }
    }
}
