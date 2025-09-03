package com.jimmy.valladares.notecoupletaker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Utilidades para ayudar al usuario a configurar correctamente la aplicación
 * en dispositivos con optimizaciones agresivas de batería (Xiaomi, Huawei, etc.)
 */
object DeviceOptimizationUtils {

    private const val TAG = "DeviceOptimization"

    /**
     * Detecta si el dispositivo es de un fabricante conocido por tener optimizaciones agresivas
     */
    fun hasAggressiveBatteryOptimization(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> true
            manufacturer.contains("huawei") -> true
            manufacturer.contains("honor") -> true
            manufacturer.contains("oppo") -> true
            manufacturer.contains("vivo") -> true
            manufacturer.contains("oneplus") -> true
            manufacturer.contains("realme") -> true
            manufacturer.contains("samsung") -> true
            else -> false
        }
    }

    /**
     * Obtiene el nombre del fabricante para mostrar instrucciones específicas
     */
    fun getManufacturerName(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> "Xiaomi (MIUI)"
            manufacturer.contains("huawei") -> "Huawei"
            manufacturer.contains("honor") -> "Honor"
            manufacturer.contains("oppo") -> "OPPO"
            manufacturer.contains("vivo") -> "Vivo"
            manufacturer.contains("oneplus") -> "OnePlus"
            manufacturer.contains("realme") -> "Realme"
            manufacturer.contains("samsung") -> "Samsung"
            else -> "Android"
        }
    }

    /**
     * Abre la configuración de optimización de batería para excluir la aplicación
     */
    fun openBatteryOptimizationSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir configuración de batería", e)
            // Fallback a configuración general de batería
            try {
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Error al abrir configuración general de batería", e2)
                false
            }
        }
    }

    /**
     * Abre la configuración de aplicaciones para configurar autostart (específico para Xiaomi)
     */
    fun openXiaomiAutoStartSettings(context: Context): Boolean {
        return try {
            // Intentar abrir la configuración de autostart de Xiaomi
            val intent = Intent().apply {
                component = android.content.ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir autostart de Xiaomi", e)
            // Fallback a configuración de aplicaciones
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Error al abrir configuración de aplicación", e2)
                false
            }
        }
    }

    /**
     * Obtiene las instrucciones específicas para el fabricante del dispositivo
     */
    fun getOptimizationInstructions(): List<String> {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> listOf(
                "1. Ve a Configuración > Aplicaciones > Gestionar aplicaciones",
                "2. Busca 'NoteCoupleTaker' y tócala",
                "3. Ve a 'Autostart' y actívalo",
                "4. Ve a 'Ahorro de batería' y selecciona 'Sin restricciones'",
                "5. Ve a 'Otras autorizaciones' > 'Mostrar en pantalla de bloqueo'",
                "6. En 'Security' > 'Boost speed' añade NoteCoupleTaker a la lista blanca"
            )
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> listOf(
                "1. Ve a Configuración > Aplicaciones > NoteCoupleTaker",
                "2. Ve a 'Batería' > 'Inicio de aplicación'",
                "3. Activa 'Gestión manual' y marca todas las opciones",
                "4. Ve a 'Batería' > 'Optimización de batería'",
                "5. Busca NoteCoupleTaker y selecciona 'No optimizar'"
            )
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> listOf(
                "1. Ve a Configuración > Batería > Optimización de energía",
                "2. Busca NoteCoupleTaker y desactiva la optimización",
                "3. Ve a Configuración > Aplicaciones > NoteCoupleTaker",
                "4. Activa 'Permitir en segundo plano'"
            )
            manufacturer.contains("vivo") -> listOf(
                "1. Ve a Configuración > Batería > Administración en segundo plano",
                "2. Busca NoteCoupleTaker y permite la ejecución en segundo plano",
                "3. Ve a 'Autostart' y activa NoteCoupleTaker"
            )
            manufacturer.contains("oneplus") -> listOf(
                "1. Ve a Configuración > Batería > Optimización de batería",
                "2. Busca NoteCoupleTaker y selecciona 'No optimizar'",
                "3. Ve a Configuración > Aplicaciones > NoteCoupleTaker > Batería",
                "4. Selecciona 'Sin optimizar'"
            )
            manufacturer.contains("samsung") -> listOf(
                "1. Ve a Configuración > Cuidado del dispositivo > Batería",
                "2. Ve a 'Más opciones de batería' > 'Optimizar uso de batería'",
                "3. Busca NoteCoupleTaker y desactiva la optimización",
                "4. Ve a 'Aplicaciones no supervisadas' y añade NoteCoupleTaker"
            )
            else -> listOf(
                "1. Ve a Configuración > Batería > Optimización de batería",
                "2. Busca NoteCoupleTaker y selecciona 'No optimizar'",
                "3. Asegúrate de que la aplicación pueda ejecutarse en segundo plano"
            )
        }
    }
}
