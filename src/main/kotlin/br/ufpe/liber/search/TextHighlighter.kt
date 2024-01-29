package br.ufpe.liber.search

import br.ufpe.liber.EagerInProduction
import gg.jte.Content
import gg.jte.html.HtmlContent
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.Fields
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query
import org.apache.lucene.search.highlight.Fragmenter
import org.apache.lucene.search.highlight.Highlighter
import org.apache.lucene.search.highlight.NullFragmenter
import org.apache.lucene.search.highlight.QueryScorer
import org.apache.lucene.search.highlight.SimpleHTMLFormatter
import org.apache.lucene.search.highlight.SimpleSpanFragmenter
import org.apache.lucene.search.highlight.TokenSources

@Singleton
@EagerInProduction
class TextHighlighter(private val analyzer: Analyzer) {
    companion object {
        const val MAX_NUM_FRAGMENTS = 4 // better for top results
        lateinit var staticAnalyzer: Analyzer // skipcq: KT-W1047

        fun highlightText(query: String, text: String): String = if (query.isBlank()) {
            text
        } else {
            val highlighter = TextHighlighter(staticAnalyzer)
            highlighter.highlightText(
                highlighter.highlighter(
                    query = highlighter.query(query),
                    fragmenter = NullFragmenter(),
                ),
                text,
                null,
            )
        }
    }

    init {
        staticAnalyzer = analyzer
    }

    private fun query(query: String): Query {
        val queryParser = QueryParser(DayMetadata.CONTENTS, analyzer)
        return queryParser.parse(query)
    }

    fun highlighter(
        query: Query,
        scorer: QueryScorer = QueryScorer(query, DayMetadata.CONTENTS),
        fragmenter: Fragmenter = SimpleSpanFragmenter(scorer),
    ): Highlighter {
        val formatter = SimpleHTMLFormatter("<mark>", "</mark>")
        val highlighter = Highlighter(formatter, scorer).apply {
            textFragmenter = fragmenter
        }

        return highlighter
    }

    private fun highlightText(highlighter: Highlighter, content: String, fields: Fields?): String {
        val tokenStream = TokenSources.getTokenStream(
            DayMetadata.CONTENTS,
            fields,
            content,
            analyzer,
            highlighter.maxDocCharsToAnalyze - 1,
        )
        return highlighter.getBestFragments(tokenStream, content, MAX_NUM_FRAGMENTS).joinToString(" ... ")
    }

    fun highlightContent(highlighter: Highlighter, content: String, fields: Fields?): Content =
        HtmlContent { it.writeContent(highlightText(highlighter, content, fields)) }
}
