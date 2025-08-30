package com.jimmy.valladares.notecoupletaker.data.repository

import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

/**
 * Repositorio que maneja los datos de compromisos
 * Por ahora contiene datos de ejemplo, en el futuro se conectará a una base de datos
 */
class CommitmentRepository {

    private val sampleCommitments = listOf(
        Commitment(
            id = "1",
            title = "Conversar 30 minutos diarios sin dispositivos",
            description = "Dedicar media hora cada día para conversar sin teléfonos ni distracciones",
            category = CommitmentCategory.COMMUNICATION,
            creationDate = LocalDateTime.now().minusDays(5)
        ),
        Commitment(
            id = "2", 
            title = "Hacer ejercicio juntos 3 veces por semana",
            description = "Realizar actividad física como pareja para mejorar nuestra salud",
            category = CommitmentCategory.HABITS,
            creationDate = LocalDateTime.now().minusDays(3)
        ),
        Commitment(
            id = "3",
            title = "Ahorrar para nuestro viaje de aniversario",
            description = "Apartar $200 cada mes para el viaje especial de nuestro aniversario",
            category = CommitmentCategory.GOALS,
            creationDate = LocalDateTime.now().minusDays(1)
        ),
        Commitment(
            id = "4",
            title = "Cita nocturna semanal",
            description = "Programar una cita especial cada viernes por la noche",
            category = CommitmentCategory.QUALITY_TIME,
            creationDate = LocalDateTime.now()
        )
    )

    /**
     * Obtiene todos los compromisos
     */
    fun getAllCommitments(): Flow<List<Commitment>> {
        return flowOf(sampleCommitments)
    }
}
