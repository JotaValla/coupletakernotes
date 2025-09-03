package com.jimmy.valladares.notecoupletaker.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utilidades para manejar las preferencias de configuración inicial de la aplicación
 */
object SetupPreferencesUtils {
    
    private const val PREFS_NAME = "setup_preferences"
    private const val KEY_INITIAL_SETUP_COMPLETED = "initial_setup_completed"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    
    /**
     * Obtiene las SharedPreferences para la configuración
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Verifica si es el primer lanzamiento de la aplicación
     * @param context Contexto de la aplicación
     * @return true si es el primer lanzamiento, false en caso contrario
     */
    fun isFirstLaunch(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    /**
     * Marca que ya no es el primer lanzamiento
     * @param context Contexto de la aplicación
     */
    fun setFirstLaunchCompleted(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .apply()
    }
    
    /**
     * Verifica si la configuración inicial ha sido completada
     * @param context Contexto de la aplicación
     * @return true si la configuración está completa, false en caso contrario
     */
    fun isInitialSetupCompleted(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_INITIAL_SETUP_COMPLETED, false)
    }
    
    /**
     * Marca la configuración inicial como completada
     * @param context Contexto de la aplicación
     */
    fun setInitialSetupCompleted(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_INITIAL_SETUP_COMPLETED, true)
            .apply()
    }
    
    /**
     * Resetea toda la configuración (útil para testing o reinstalación)
     * @param context Contexto de la aplicación
     */
    fun resetSetup(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_INITIAL_SETUP_COMPLETED, false)
            .putBoolean(KEY_FIRST_LAUNCH, true)
            .apply()
    }
    
    /**
     * Verifica si se debe mostrar la pantalla de configuración inicial
     * @param context Contexto de la aplicación
     * @return true si se debe mostrar la configuración, false en caso contrario
     */
    fun shouldShowInitialSetup(context: Context): Boolean {
        // Mostrar si es primer lanzamiento o si la configuración no está completa
        // o si faltan permisos esenciales
        return isFirstLaunch(context) || 
               !isInitialSetupCompleted(context) || 
               !NotificationPermissionUtils.areAllPermissionsGranted(context)
    }
}
