package com.jimmy.valladares.notecoupletaker.utils

import android.Manifest
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.jimmy.valladares.notecoupletaker.service.KeepAliveService

/**
 * Utilidades para gestionar los permisos de acceso a notificaciones
 */
object NotificationPermissionUtils {

    /**
     * Verifica si la aplicación tiene permiso para acceder a las notificaciones del sistema
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
     */
    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Verifica si el servicio de captura de notificaciones está actualmente conectado
     */
    fun isNotificationServiceConnected(context: Context): Boolean {
        return isNotificationListenerEnabled(context)
    }

    /**
     * Inicia el servicio en primer plano para mantener la aplicación activa
     */
    fun startKeepAliveServiceIfNeeded(context: Context) {
        if (isNotificationListenerEnabled(context)) {
            try {
                val intent = Intent(context, KeepAliveService::class.java)
                context.startForegroundService(intent)
                
                // Programar el watchdog para monitorear el servicio
                WatchdogReceiver.scheduleWatchdog(context)
            } catch (e: Exception) {
                // En caso de error, no hacer nada crítico
            }
        }
    }

    /**
     * Detiene el servicio en primer plano cuando el acceso a notificaciones es deshabilitado
     */
    fun stopKeepAliveService(context: Context) {
        try {
            val intent = Intent(context, KeepAliveService::class.java)
            context.stopService(intent)
            
            // Cancelar el watchdog ya que no necesitamos el servicio
            WatchdogReceiver.cancelWatchdog(context)
        } catch (e: Exception) {
            // En caso de error, no hacer nada crítico
        }
    }

    /**
     * Verifica si la aplicación tiene permiso para mostrar notificaciones (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Verifica si la aplicación tiene permiso para programar alarmas exactas
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Verifica si la optimización de batería está habilitada para la aplicación
     */
    fun isBatteryOptimizationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            !isIgnored
        } else {
            false
        }
    }

    /**
     * Solicita deshabilitar la optimización de batería para la aplicación
     */
    fun requestDisableBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openBatteryOptimizationSettings(context)
            }
        }
    }

    /**
     * Abre la configuración general de optimización de batería
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    /**
     * Abre la configuración de la aplicación
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Verifica si todos los permisos esenciales están concedidos
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        val hasNotification = hasNotificationPermission(context)
        val hasListener = isNotificationListenerEnabled(context)
        val hasAlarm = hasExactAlarmPermission(context)
        val batteryOptimized = isBatteryOptimizationEnabled(context)
        
        return hasNotification && hasListener && hasAlarm && !batteryOptimized
    }
}
