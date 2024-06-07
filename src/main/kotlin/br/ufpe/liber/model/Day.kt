package br.ufpe.liber.model

import io.micronaut.serde.annotation.Serdeable
import kotlinx.serialization.Serializable

@Serdeable
@Serializable
data class Day(val id: Long, val day: String, val contents: String)
