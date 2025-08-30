package com.jimmy.valladares.notecoupletaker.di

import android.content.Context
import com.jimmy.valladares.notecoupletaker.data.database.AppDatabase
import com.jimmy.valladares.notecoupletaker.data.database.CommitmentDao
import com.jimmy.valladares.notecoupletaker.data.repository.CommitmentRepository

/**
 * Contenedor simple de dependencias para la aplicación
 * En el futuro se puede reemplazar por Hilt o Dagger
 */
class AppContainer(context: Context) {

    /**
     * Base de datos de la aplicación
     */
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    /**
     * DAO para acceso a datos de compromisos
     */
    val commitmentDao: CommitmentDao by lazy {
        database.commitmentDao()
    }

    /**
     * Repositorio de compromisos
     */
    val commitmentRepository: CommitmentRepository by lazy {
        CommitmentRepository(commitmentDao)
    }
}
