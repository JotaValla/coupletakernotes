package com.jimmy.valladares.notecoupletaker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jimmy.valladares.notecoupletaker.NoteCoupleTakerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver que restaura las alarmas después de un reinicio del dispositivo
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // Restaurar todas las alarmas de recordatorios
            restoreReminders(context)
            
            // Iniciar el servicio KeepAlive si tenemos permisos de notificación
            NotificationPermissionUtils.startKeepAliveServiceIfNeeded(context)
        }
    }
    
    private fun restoreReminders(context: Context) {
        val application = context.applicationContext as? NoteCoupleTakerApplication
        val repository = application?.appContainer?.commitmentRepository
        val notificationScheduler = NotificationScheduler(context)
        
        repository?.let { repo ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Obtener todos los compromisos con recordatorios configurados
                    repo.getAllCommitments().collect { commitments ->
                        commitments.forEach { commitment ->
                            commitment.reminderTime?.let { reminderTime ->
                                notificationScheduler.scheduleReminder(
                                    commitmentId = commitment.id,
                                    commitmentTitle = commitment.title,
                                    timeString = reminderTime
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Log error but don't crash
                    e.printStackTrace()
                }
            }
        }
    }
}
