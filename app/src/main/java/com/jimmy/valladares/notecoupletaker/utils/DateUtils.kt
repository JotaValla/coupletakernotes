package com.jimmy.valladares.notecoupletaker.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilidades para formatear fechas y timestamps de manera amigable para el usuario
 */
object DateUtils {
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    
    /**
     * Formatea un timestamp de manera amigable para el usuario
     * 
     * @param timestamp El timestamp a formatear (puede ser Date o Long)
     * @return String formateado según las reglas:
     *         - Si es de hoy: "HH:mm"
     *         - Si es de ayer: "Ayer, HH:mm"
     *         - Si es de esta semana: "Lunes, HH:mm"
     *         - Si es más antiguo: "dd MMM, HH:mm"
     */
    fun formatNotificationTime(timestamp: Any?): String {
        val date = when (timestamp) {
            is Date -> timestamp
            is Long -> Date(timestamp)
            is com.google.firebase.Timestamp -> timestamp.toDate()
            else -> return "Fecha desconocida"
        }
        
        val now = Calendar.getInstance()
        val notificationCalendar = Calendar.getInstance().apply { time = date }
        
        // Verificar si es del mismo día
        if (isSameDay(now, notificationCalendar)) {
            return timeFormat.format(date)
        }
        
        // Verificar si es de ayer
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        if (isSameDay(yesterday, notificationCalendar)) {
            return "Ayer, ${timeFormat.format(date)}"
        }
        
        // Verificar si es de esta semana (últimos 7 días)
        val weekAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }
        if (notificationCalendar.after(weekAgo)) {
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
            return "$dayOfWeek, ${timeFormat.format(date)}"
        }
        
        // Para fechas más antiguas
        return "${dayMonthFormat.format(date)}, ${timeFormat.format(date)}"
    }
    
    /**
     * Verifica si dos calendarios representan el mismo día
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Formatea una fecha completa para mostrar información detallada
     */
    fun formatFullDateTime(timestamp: Any?): String {
        val date = when (timestamp) {
            is Date -> timestamp
            is Long -> Date(timestamp)
            is com.google.firebase.Timestamp -> timestamp.toDate()
            else -> return "Fecha desconocida"
        }
        
        return dateTimeFormat.format(date)
    }
}
