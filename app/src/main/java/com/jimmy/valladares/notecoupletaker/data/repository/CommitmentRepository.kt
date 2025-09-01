package com.jimmy.valladares.notecoupletaker.data.repository

import com.jimmy.valladares.notecoupletaker.data.database.CommitmentDao
import com.jimmy.valladares.notecoupletaker.domain.model.ChecklistItem
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentWithChecklist
import com.jimmy.valladares.notecoupletaker.domain.model.generateDefaultChecklistItems
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que maneja los datos de compromisos usando Room como persistencia
 */
class CommitmentRepository(
    private val commitmentDao: CommitmentDao
) {

    /**
     * Obtiene todos los compromisos con sus checklists
     */
    fun getAllCommitmentsWithChecklist(): Flow<List<CommitmentWithChecklist>> {
        return commitmentDao.getAllCommitmentsWithChecklist()
    }

    /**
     * Obtiene un compromiso específico por ID con su checklist
     */
    fun getCommitmentWithChecklistById(id: Int): Flow<CommitmentWithChecklist?> {
        return commitmentDao.getCommitmentWithChecklistById(id)
    }

    /**
     * Agrega un nuevo compromiso a la base de datos con su checklist por defecto
     */
    suspend fun addCommitment(commitment: Commitment) {
        // Insertar el compromiso y obtener su ID generado
        val commitmentId = commitmentDao.insertCommitment(commitment)
        
        // Generar y insertar los ítems del checklist por defecto
        val checklistItems = generateDefaultChecklistItems(commitmentId.toInt())
        commitmentDao.insertChecklistItems(checklistItems)
    }

    /**
     * Actualiza un compromiso existente
     */
    suspend fun updateCommitment(commitment: Commitment) {
        commitmentDao.updateCommitment(commitment)
    }

    /**
     * Actualiza el estado de un ítem del checklist
     */
    suspend fun updateChecklistItem(commitmentId: Int, checklistItemId: Int, isChecked: Boolean) {
        // Primero obtenemos el ítem actual
        val commitment = commitmentDao.getCommitmentById(commitmentId)
        if (commitment != null) {
            // Obtener todos los ítems del checklist para este compromiso
            val checklistItems = commitmentDao.getChecklistItemsByCommitmentId(commitmentId)
            
            // Como necesitamos el ítem específico, creamos uno nuevo con los datos actualizados
            val updatedItem = ChecklistItem(
                id = checklistItemId,
                commitmentId = commitmentId,
                text = "", // Se mantendrá el valor actual en la base de datos
                isChecked = isChecked
            )
            
            // Actualizamos solo el estado isChecked
            commitmentDao.updateChecklistItem(updatedItem)
        }
    }

    /**
     * Actualiza un ítem completo del checklist
     */
    suspend fun updateChecklistItem(item: ChecklistItem) {
        commitmentDao.updateChecklistItem(item)
    }

    /**
     * Elimina un compromiso
     */
    suspend fun deleteCommitment(id: Int) {
        commitmentDao.deleteCommitment(id)
    }

    /**
     * Obtiene un compromiso por ID (sin checklist)
     */
    suspend fun getCommitmentById(id: Int): Commitment? {
        return commitmentDao.getCommitmentById(id)
    }

    /**
     * Obtiene todos los compromisos (sin checklist)
     */
    fun getAllCommitments(): Flow<List<Commitment>> {
        return commitmentDao.getAllCommitments()
    }

    /**
     * Actualiza solo el tiempo de recordatorio de un compromiso
     */
    suspend fun updateReminderTime(commitmentId: Int, reminderTime: String?) {
        commitmentDao.updateReminderTime(commitmentId, reminderTime)
    }
}
