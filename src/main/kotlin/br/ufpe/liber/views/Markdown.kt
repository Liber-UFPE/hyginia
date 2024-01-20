package br.ufpe.liber.views

import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import gg.jte.html.HtmlContent

object Markdown {
    fun toHtml(content: String): HtmlContent {
        val options = MutableDataSet()
        options[Parser.EXTENSIONS] = listOf(FootnoteExtension.create())

        val parser: Parser = Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()
        val document = parser.parse(content)
        val html = renderer.render(document)
        return HtmlContent { output -> output.writeContent(html) }
    }
}
