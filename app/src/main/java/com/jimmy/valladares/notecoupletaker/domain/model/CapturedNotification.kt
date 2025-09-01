package com.jimmy.valladares.notecoupletaker.domain.model

import com.google.firebase.firestore.FieldValue

/**
 * Modelo de datos para representar una notificación capturada que se guarda en Firestore.
 * 
 * Esta clase representa el documento que se almacena en la colección "notifications" de Firestore
 * cada vez que el dispositivo recibe una nueva notificación.
 */
data class CapturedNotification(
    /**
     * El nombre del paquete de la aplicación que envió la notificación
     */
    val packageName: String = "",
    
    /**
     * El título de la notificación (puede ser null si no está disponible)
     */
    val title: String? = null,
    
    /**
     * El contenido de texto de la notificación (puede ser null si no está disponible)
     */
    val text: String? = null,
    
    /**
     * Timestamp del servidor de cuando se recibió la notificación
     * Se utiliza FieldValue.serverTimestamp() para obtener el tiempo del servidor de Firebase
     */
    val timestamp: Any = FieldValue.serverTimestamp()
)
