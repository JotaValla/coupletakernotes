package com.jimmy.valladares.notecoupletaker.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jimmy.valladares.notecoupletaker.domain.model.CapturedNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio responsable de toda la comunicación con Firestore para las notificaciones capturadas.
 * 
 * Esta clase maneja el almacenamiento y recuperación de notificaciones en la base de datos
 * de Cloud Firestore.
 */
class NotificationRepository {
    
    companion object {
        private const val TAG = "NotificationRepository"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
    }
    
    val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Guarda una notificación capturada en Firestore.
     * 
     * @param notification La notificación que se va a guardar en la base de datos
     */
    suspend fun saveNotification(notification: CapturedNotification) {
        try {
            Log.d(TAG, "🔄 Iniciando guardado en Firestore...")
            Log.d(TAG, "📦 Package: ${notification.packageName}")
            Log.d(TAG, "📋 Title: ${notification.title}")
            Log.d(TAG, "📝 Text: ${notification.text}")
            
            // Crear un mapa con los datos para mejor debugging
            val notificationData = mapOf(
                "packageName" to notification.packageName,
                "title" to notification.title,
                "text" to notification.text,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            Log.d(TAG, "📊 Datos a guardar: $notificationData")
            
            // Añadir el documento a la colección "notifications"
            val documentRef = firestore.collection(NOTIFICATIONS_COLLECTION)
                .add(notificationData)
                .await()
            
            Log.d(TAG, "✅ Notificación guardada exitosamente en Firestore con ID: ${documentRef.id}")
            Log.d(TAG, "🔗 URL: https://console.firebase.google.com/project/${firestore.app.options.projectId}/firestore/data")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar la notificación en Firestore", e)
            Log.e(TAG, "🔍 Detalles del error: ${e.message}")
            Log.e(TAG, "📊 Notification data: packageName=${notification.packageName}, title=${notification.title}")
            // En un entorno de producción, podrías enviar este error a un servicio de analytics
            // como Firebase Crashlytics para monitorear errores
        }
    }
    
    /**
     * Recupera todas las notificaciones guardadas desde Firestore (para uso futuro).
     * 
     * @return Lista de notificaciones capturadas ordenadas por timestamp descendente
     */
    suspend fun getAllNotifications(): List<CapturedNotification> {
        return try {
            Log.d(TAG, "Recuperando todas las notificaciones desde Firestore")
            
            val result = firestore.collection(NOTIFICATIONS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val notifications = result.documents.mapNotNull { document ->
                try {
                    document.toObject(CapturedNotification::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    Log.w(TAG, "Error al convertir documento a CapturedNotification: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Se recuperaron ${notifications.size} notificaciones desde Firestore")
            notifications
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al recuperar notificaciones desde Firestore", e)
            emptyList()
        }
    }
    
    /**
     * Observa las notificaciones en tiempo real desde Firestore.
     * 
     * @return Flow que emite actualizaciones en tiempo real de la lista de notificaciones
     */
    fun observeNotifications(): Flow<List<CapturedNotification>> = callbackFlow {
        Log.d(TAG, "Iniciando observación en tiempo real de notificaciones")
        
        val listener = firestore.collection(NOTIFICATIONS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al observar notificaciones en tiempo real", error)
                    // En caso de error, emitir una lista vacía
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val notifications = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(CapturedNotification::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error al convertir documento en tiempo real: ${document.id}", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Actualizando notificaciones en tiempo real: ${notifications.size} elementos")
                trySend(notifications)
            }
        
        // Cleanup cuando el Flow es cancelado
        awaitClose {
            Log.d(TAG, "Cancelando observación en tiempo real de notificaciones")
            listener.remove()
        }
    }
}
