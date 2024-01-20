package br.ufpe.liber.model

import io.micronaut.serde.annotation.Serdeable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Optional
import java.util.SortedMap
import java.util.TreeMap
import kotlin.math.min

@Serdeable
@Serializable
data class Book(
    val id: Long,
    val author: String,
    val title: String,
    val number: Int,
    val year: String,
    val period: String,
    val days: List<Day>,
) {

    companion object {
        private const val MAX_DESCRIPTION_LENGHT = 300
    }

    @Transient
    private val daysMap: SortedMap<Long, Day> = TreeMap()

    @Transient
    val daysSize: Int = days.size

    init {
        days.forEach { day ->
            daysMap[day.id] = day
        }
    }

    fun day(id: Long): Optional<Day> = Optional.ofNullable(daysMap[id])
    fun firstDay(): Day = days.first()
    fun hasNextDay(id: Long): Boolean = daysMap.containsKey(id + 1)
    fun nextDay(id: Long): Optional<Day> = Optional.ofNullable(daysMap[id + 1])
    fun hasPreviousDay(id: Long): Boolean = daysMap.containsKey(id - 1)
    fun previousDay(id: Long): Optional<Day> = Optional.ofNullable(daysMap[id - 1])

    fun description(): String {
        val firstDayContents = firstDay().contents
        val index = min(MAX_DESCRIPTION_LENGHT, firstDayContents.length)
        return firstDayContents.substring(0, index).substringBeforeLast(" ")
    }
}
