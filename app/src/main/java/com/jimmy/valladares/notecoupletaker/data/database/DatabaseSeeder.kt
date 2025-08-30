package com.jimmy.valladares.notecoupletaker.data.database

import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import com.jimmy.valladares.notecoupletaker.domain.model.generateDefaultChecklistItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Utilidad para poblar la base de datos con datos iniciales
 */
class DatabaseSeeder(private val commitmentDao: CommitmentDao) {

    /**
     * Puebla la base de datos con compromisos de ejemplo si está vacía
     */
    suspend fun seedDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            // Verificar si ya hay datos
            val existingCommitments = commitmentDao.getAllCommitments()
            
            // Si no hay compromisos, agregar datos de ejemplo
            // (Para simplificar, siempre agregaremos datos de ejemplo en la primera ejecución)
            val sampleCommitments = listOf(
                Commitment(
                    title = "Conversar 30 minutos diarios sin dispositivos",
                    description = "Dedicar media hora cada día para conversar sin teléfonos ni distracciones",
                    category = CommitmentCategory.COMMUNICATION,
                    creationDate = LocalDateTime.now().minusDays(5)
                ),
                Commitment(
                    title = "Hacer ejercicio juntos 3 veces por semana",
                    description = "Realizar actividad física como pareja para mejorar nuestra salud",
                    category = CommitmentCategory.HABITS,
                    creationDate = LocalDateTime.now().minusDays(3)
                ),
                Commitment(
                    title = "Ahorrar para nuestro viaje de aniversario",
                    description = "Apartar $200 cada mes para el viaje especial de nuestro aniversario",
                    category = CommitmentCategory.GOALS,
                    creationDate = LocalDateTime.now().minusDays(1)
                ),
                Commitment(
                    title = "Cita nocturna semanal",
                    description = "Programar una cita especial cada viernes por la noche",
                    category = CommitmentCategory.QUALITY_TIME,
                    creationDate = LocalDateTime.now()
                )
            )

            // Insertar cada compromiso con su checklist
            sampleCommitments.forEach { commitment ->
                val commitmentId = commitmentDao.insertCommitment(commitment)
                val checklistItems = generateDefaultChecklistItems(commitmentId.toInt())
                commitmentDao.insertChecklistItems(checklistItems)
            }
        }
    }
}
