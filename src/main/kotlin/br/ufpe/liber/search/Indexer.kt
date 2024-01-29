package br.ufpe.liber.search

import br.ufpe.liber.EagerInProduction
import br.ufpe.liber.services.BookRepository
import jakarta.inject.Singleton
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
@EagerInProduction
class Indexer(
    private val bookRepository: BookRepository,
    private val directory: Directory,
    private val analyzer: Analyzer,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Indexer::class.java)
    }

    private fun richTextField(name: String, content: String): Field {
        val fieldType = FieldType()
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS)
        fieldType.setStored(true)
        fieldType.setTokenized(true)
        fieldType.setOmitNorms(false)

        fieldType.setStoreTermVectors(true) // captures frequencies
        fieldType.setStoreTermVectorPositions(true) // depends on freqs
        fieldType.setStoreTermVectorOffsets(true) // depends on freqs

        return Field(name, content, fieldType)
    }

    init {
        logger.info("Starting to index all books")
        IndexWriter(directory, IndexWriterConfig(analyzer)).use { writer ->
            runBlocking {
                bookRepository.listAll().forEach { book ->
                    launch {
                        logger.info("Indexing book with id ${book.id}")
                        book.days.forEach { day ->
                            val doc = Document()
                            doc.add(StoredField(BookMetadata.ID, book.id))
                            doc.add(StoredField(BookMetadata.AUTHOR, book.author))
                            doc.add(StoredField(BookMetadata.TITLE, book.title))
                            doc.add(StoredField(BookMetadata.NUMBER, book.number))
                            doc.add(StoredField(BookMetadata.YEAR, book.year))
                            doc.add(StoredField(BookMetadata.PERIOD, book.period))

                            doc.add(StoredField(DayMetadata.ID, day.id))
                            doc.add(TextField(DayMetadata.DAY, day.day, Field.Store.YES))
                            doc.add(richTextField(DayMetadata.CONTENTS, day.contents))

                            writer.addDocument(doc)
                        }
                    }
                }
            }
        }
    }
}
