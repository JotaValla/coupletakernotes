package com.jimmy.valladares.notecoupletaker

import android.app.Application
import com.jimmy.valladares.notecoupletaker.di.AppContainer

/**
 * Clase Application personalizada para inicializar dependencias
 */
class NoteCoupleTakerApplication : Application() {

    /**
     * Contenedor de dependencias de la aplicación
     */
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
