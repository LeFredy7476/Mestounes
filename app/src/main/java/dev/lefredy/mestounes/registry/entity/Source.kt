package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

enum class SourceType {
    LOCAL,
    DISTANT,
    HYBRID
}

@Entity
data class Source(
    @PrimaryKey val sourceId: Int,
    val name: String,
    val type: SourceType,
    val local: String,
    val distant: String
)