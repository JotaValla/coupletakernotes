package com.jimmy.valladares.notecoupletaker.data.repository

import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import com.jimmy.valladares.notecoupletaker.domain.model.ChecklistItem
import com.jimmy.valladares.notecoupletaker.domain.model.generateDefaultChecklist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * Repositorio que maneja los datos de compromisos
 * Por ahora contiene datos de ejemplo, en el futuro se conectará a una base de datos
 */
class CommitmentRepository {

    private val initialCommitments = listOf(
        Commitment(
            id = "1",
            title = "Conversar 30 minutos diarios sin dispositivos",
            description = "Dedicar media hora cada día para conversar sin teléfonos ni distracciones",
            category = CommitmentCategory.COMMUNICATION,
            creationDate = LocalDateTime.now().minusDays(5),
            checklist = generateDefaultChecklist()
        ),
        Commitment(
            id = "2", 
            title = "Hacer ejercicio juntos 3 veces por semana",
            description = "Realizar actividad física como pareja para mejorar nuestra salud",
            category = CommitmentCategory.HABITS,
            creationDate = LocalDateTime.now().minusDays(3),
            checklist = generateDefaultChecklist()
        ),
        Commitment(
            id = "3",
            title = "Ahorrar para nuestro viaje de aniversario",
            description = "Apartar $200 cada mes para el viaje especial de nuestro aniversario",
            category = CommitmentCategory.GOALS,
            creationDate = LocalDateTime.now().minusDays(1),
            checklist = generateDefaultChecklist()
        ),
        Commitment(
            id = "4",
            title = "Cita nocturna semanal",
            description = "Programar una cita especial cada viernes por la noche",
            category = CommitmentCategory.QUALITY_TIME,
            creationDate = LocalDateTime.now(),
            checklist = generateDefaultChecklist()
        )
    )

    // Lista mutable para mantener los compromisos en memoria
    private val _commitments = MutableStateFlow(initialCommitments)

    /**
     * Obtiene todos los compromisos
     */
    fun getAllCommitments(): Flow<List<Commitment>> {
        return _commitments.asStateFlow()
    }

    /**
     * Agrega un nuevo compromiso a la lista
     */
    suspend fun addCommitment(commitment: Commitment) {
        val currentList = _commitments.value.toMutableList()
        currentList.add(commitment)
        _commitments.value = currentList
    }

    /**
     * Obtiene un compromiso por su ID
     */
    fun getCommitmentById(id: String): Commitment? {
        return _commitments.value.find { it.id == id }
    }

    /**
     * Actualiza el estado de un ítem del checklist
     */
    suspend fun updateChecklistItem(commitmentId: String, checklistItemId: String, isChecked: Boolean) {
        val currentList = _commitments.value.toMutableList()
        val commitmentIndex = currentList.indexOfFirst { it.id == commitmentId }
        
        if (commitmentIndex != -1) {
            val commitment = currentList[commitmentIndex]
            val updatedChecklist = commitment.checklist.map { item ->
                if (item.id == checklistItemId) {
                    item.copy(isChecked = isChecked)
                } else {
                    item
                }
            }
            
            val updatedCommitment = commitment.copy(checklist = updatedChecklist)
            currentList[commitmentIndex] = updatedCommitment
            _commitments.value = currentList
        }
    }
}
