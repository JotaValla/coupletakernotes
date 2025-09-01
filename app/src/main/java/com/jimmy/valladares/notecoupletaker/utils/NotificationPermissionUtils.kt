package com.jimmy.valladares.notecoupletaker.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

/**
 * Utilidades para gestionar los permisos de acceso a notificaciones
 */
object NotificationPermissionUtils {

    /**
     * Verifica si la aplicación tiene permiso para acceder a las notificaciones del sistema
     * @param context Contexto de la aplicación
     * @return true si el permiso está concedido, false en caso contrario
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                val nameMatch = componentName?.packageName == packageName
                if (nameMatch) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Abre la pantalla de configuración de acceso a notificaciones del sistema
     * @param context Contexto de la aplicación
     */
    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Verifica si el servicio de captura de notificaciones está actualmente conectado
     * @param context Contexto de la aplicación
     * @return true si el servicio está conectado, false en caso contrario
     */
    fun isNotificationServiceConnected(context: Context): Boolean {
        // Esta verificación se basa en si el servicio tiene los permisos necesarios
        // En una implementación más avanzada, podríamos tener un mecanismo de heartbeat
        return isNotificationListenerEnabled(context)
    }
}
