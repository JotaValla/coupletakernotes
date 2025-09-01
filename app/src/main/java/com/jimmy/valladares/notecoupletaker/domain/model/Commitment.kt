package com.jimmy.valladares.notecoupletaker.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de dominio que representa un compromiso o meta de pareja
 */
@Entity(tableName = "commitments")
data class Commitment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: CommitmentCategory,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val reminderTime: String? = null // Hora del recordatorio en formato "HH:mm"
)

/**
 * Modelo de dominio que representa un ítem del checklist de un compromiso
 */
@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = Commitment::class,
            parentColumns = ["id"],
            childColumns = ["commitmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val commitmentId: Int,
    val text: String,
    val isChecked: Boolean = false
)

/**
 * Clase de relación para obtener un compromiso con su lista de checklist
 */
data class CommitmentWithChecklist(
    @Embedded val commitment: Commitment,
    @Relation(
        parentColumn = "id",
        entityColumn = "commitmentId"
    )
    val checklist: List<ChecklistItem>
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

/**
 * Genera una lista de checklist por defecto para un nuevo compromiso
 */
fun generateDefaultChecklistItems(commitmentId: Int): List<ChecklistItem> {
    return (1..7).map { day ->
        ChecklistItem(
            commitmentId = commitmentId,
            text = "Día $day completado",
            isChecked = false
        )
    }
}
