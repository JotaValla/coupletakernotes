package com.jimmy.valladares.notecoupletaker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jimmy.valladares.notecoupletaker.domain.model.ChecklistItem
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentWithChecklist
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para interactuar con la base de datos de compromisos
 */
@Dao
interface CommitmentDao {

    /**
     * Inserta un nuevo compromiso y retorna su ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: Commitment): Long

    /**
     * Inserta una lista de ítems del checklist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItem>)

    /**
     * Actualiza un compromiso existente
     */
    @Update
    suspend fun updateCommitment(commitment: Commitment)

    /**
     * Actualiza un ítem del checklist
     */
    @Update
    suspend fun updateChecklistItem(item: ChecklistItem)

    /**
     * Obtiene todos los compromisos con sus respectivos checklists
     */
    @Transaction
    @Query("SELECT * FROM commitments ORDER BY creationDate DESC")
    fun getAllCommitmentsWithChecklist(): Flow<List<CommitmentWithChecklist>>

    /**
     * Obtiene un compromiso específico por ID con su checklist
     */
    @Transaction
    @Query("SELECT * FROM commitments WHERE id = :id")
    fun getCommitmentWithChecklistById(id: Int): Flow<CommitmentWithChecklist?>

    /**
     * Obtiene todos los compromisos sin checklist
     */
    @Query("SELECT * FROM commitments ORDER BY creationDate DESC")
    fun getAllCommitments(): Flow<List<Commitment>>

    /**
     * Obtiene un compromiso específico por ID
     */
    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getCommitmentById(id: Int): Commitment?

    /**
     * Obtiene todos los ítems del checklist para un compromiso específico
     */
    @Query("SELECT * FROM checklist_items WHERE commitmentId = :commitmentId")
    fun getChecklistItemsByCommitmentId(commitmentId: Int): Flow<List<ChecklistItem>>

    /**
     * Elimina un compromiso (también elimina automáticamente sus checklist items por la FK)
     */
    @Query("DELETE FROM commitments WHERE id = :id")
    suspend fun deleteCommitment(id: Int)

    /**
     * Elimina todos los compromisos (útil para testing)
     */
    @Query("DELETE FROM commitments")
    suspend fun deleteAllCommitments()
}
