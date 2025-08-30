package com.jimmy.valladares.notecoupletaker.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de dominio que representa un compromiso o meta de pareja
 */
data class Commitment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: CommitmentCategory,
    val creationDate: LocalDateTime = LocalDateTime.now()
)

/**
 * Enum que define las categorías disponibles para los compromisos
 */
enum class CommitmentCategory(val displayName: String, val iconRes: String) {
    COMMUNICATION("Comunicación", "💬"),
    HABITS("Hábitos", "🔄"),
    GOALS("Metas", "🎯"),
    QUALITY_TIME("Tiempo de Calidad", "❤️"),
    PERSONAL_GROWTH("Crecimiento Personal", "🌱")
}
