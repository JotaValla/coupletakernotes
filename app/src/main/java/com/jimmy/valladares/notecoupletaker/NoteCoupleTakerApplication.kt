package com.jimmy.valladares.notecoupletaker

import android.app.Application
import com.google.firebase.FirebaseApp
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
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        appContainer = AppContainer(this)
    }
}
