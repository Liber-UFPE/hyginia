package br.ufpe.liber.search

import br.ufpe.liber.model.Book
import br.ufpe.liber.model.Day
import br.ufpe.liber.pagination.Pagination
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
import org.apache.lucene.search.TopScoreDocCollector
import org.reactivestreams.Publisher
import java.util.concurrent.ExecutorService
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Singleton
class BooksSearch(
    private val indexSearcher: IndexSearcher,
    private val analyzer: Analyzer,
    private val textHighlighter: TextHighlighter,
) {

    companion object {
        const val RESULTS_PER_PAGE: Int = 10
        const val MAX_HITS_THRESHOLD: Int = 1000
    }

    fun search(keywords: String, page: Int = 0): SearchResults {
        val queryParser = QueryParser(DayMetadata.CONTENTS, analyzer)
        val query = queryParser.parse(keywords)
        val highlighter = textHighlighter.highlighter(query)

        val storedFields = indexSearcher.storedFields()
        val indexReader = indexSearcher.indexReader
        val termVectors = indexReader.termVectors()

        val collector = TopScoreDocCollector.create(MAX_HITS_THRESHOLD, MAX_HITS_THRESHOLD + 1)
        indexSearcher.search(query, collector)
        val topDocs = collector.topDocs()

        if (topDocs.totalHits.value == 0L) return SearchResults.empty()

        val pagingStart: Int = max(page, 0) * RESULTS_PER_PAGE
        val pagingEnd: Int = min(pagingStart + RESULTS_PER_PAGE, topDocs.totalHits.value.toInt() - 1)

        val searchResults = topDocs.scoreDocs.slice(pagingStart..pagingEnd).map { scoreDoc: ScoreDoc ->
            val document = storedFields.document(scoreDoc.doc)
            val dayContents = document.get(DayMetadata.CONTENTS)

            val fields = termVectors.get(scoreDoc.doc)
            val hightlightedContent = textHighlighter.highlightContent(highlighter, dayContents, fields)

            SearchResult(document, hightlightedContent)
        }

        return SearchResults(topDocs.totalHits.value.toInt(), searchResults, page + 1)
    }
}

@Serdeable
data class SearchResult(
    val book: Book,
    val day: Day,
    val hightlightedContent: Content,
) {
    constructor(doc: Document, hightlightedContent: Content) : this(
        Book(
            id = doc.get(BookMetadata.ID).toLong(),
            author = doc.get(BookMetadata.AUTHOR),
            title = doc.get(BookMetadata.TITLE),
            number = doc.get(BookMetadata.NUMBER).toInt(),
            year = doc.get(BookMetadata.YEAR),
            period = doc.get(BookMetadata.PERIOD),
            days = listOf(),
        ),
        Day(
            id = doc.get(DayMetadata.ID).toLong(),
            day = doc.get(DayMetadata.DAY),
            contents = doc.get(DayMetadata.CONTENTS),
        ),
        hightlightedContent,
    )
}

@Serdeable
data class SearchResults(
    val hits: Int = 0,
    val items: List<SearchResult> = listOf(),
    val currentPage: Int = Pagination.FIRST,
) : Collection<SearchResult> by items {
    companion object {
        fun empty() = SearchResults()
    }

    val totalPages: Int = ceil(hits.toDouble() / BooksSearch.RESULTS_PER_PAGE).toInt()

    val isFirstPage: Boolean = currentPage == Pagination.FIRST
    val isLastPage: Boolean = currentPage == totalPages

    val pages = Pagination(currentPage, totalPages).listPages()
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
