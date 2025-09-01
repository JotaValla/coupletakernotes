package com.jimmy.valladares.notecoupletaker.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimmy.valladares.notecoupletaker.data.repository.NotificationRepository
import com.jimmy.valladares.notecoupletaker.domain.model.CapturedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de historial de notificaciones.
 * Maneja el estado de la UI y la obtención de datos desde Firestore.
 */
class NotificationHistoryViewModel : ViewModel() {
    
    private val notificationRepository = NotificationRepository()
    
    // Estado interno mutable
    private val _uiState = MutableStateFlow(NotificationHistoryUiState())
    
    // Estado expuesto a la UI (inmutable)
    val uiState: StateFlow<NotificationHistoryUiState> = _uiState.asStateFlow()
    
    init {
        // Iniciar la observación de notificaciones en tiempo real
        observeNotifications()
    }
    
    /**
     * Observa las notificaciones en tiempo real desde Firestore
     */
    private fun observeNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            notificationRepository.observeNotifications()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar notificaciones: ${error.message}",
                        notifications = emptyList()
                    )
                }
                .collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        notifications = notifications
                    )
                }
        }
    }
    
    /**
     * Refresca manualmente las notificaciones
     */
    fun refreshNotifications() {
        observeNotifications()
    }
}

/**
 * Estado de la UI para la pantalla de historial de notificaciones
 */
data class NotificationHistoryUiState(
    val isLoading: Boolean = false,
    val notifications: List<CapturedNotification> = emptyList(),
    val error: String? = null
)
