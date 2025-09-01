package com.jimmy.valladares.notecoupletaker.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimmy.valladares.notecoupletaker.R
import com.jimmy.valladares.notecoupletaker.domain.model.CapturedNotification
import com.jimmy.valladares.notecoupletaker.ui.theme.NoteCoupleTakerTheme
import com.jimmy.valladares.notecoupletaker.utils.DateUtils

/**
 * Pantalla que muestra el historial de notificaciones capturadas desde Firestore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backButtonContentDescription = stringResource(R.string.cd_back_button)
    val refreshContentDescription = stringResource(R.string.cd_refresh_notifications)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notification_history_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { 
                            contentDescription = backButtonContentDescription 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backButtonContentDescription
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshNotifications() },
                        modifier = Modifier.semantics { 
                            contentDescription = refreshContentDescription 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = refreshContentDescription
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.refreshNotifications() }
                )
            }
            uiState.notifications.isEmpty() -> {
                EmptyStateContent()
            }
            else -> {
                NotificationsList(
                    notifications = uiState.notifications,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Lista de notificaciones
 */
@Composable
private fun NotificationsList(
    notifications: List<CapturedNotification>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(
                    R.string.notification_history_count,
                    notifications.size
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(
            items = notifications,
            key = { notification ->
                // Usar el ID de Firestore como clave única y estable
                notification.id
            }
        ) { notification ->
            NotificationCard(notification = notification)
        }
    }
}

/**
 * Tarjeta individual para cada notificación
 */
@Composable
private fun NotificationCard(
    notification: CapturedNotification,
    modifier: Modifier = Modifier
) {
    val cardContentDescription = stringResource(R.string.cd_notification_card)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardContentDescription },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Ícono de la aplicación (placeholder)
            AppIconPlaceholder(
                packageName = notification.packageName,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Contenido de la notificación
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre de la app y timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getAppDisplayName(notification.packageName),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = DateUtils.formatNotificationTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Título de la notificación
                if (!notification.title.isNullOrBlank()) {
                    Text(
                        text = notification.title!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                // Texto de la notificación
                if (!notification.text.isNullOrBlank()) {
                    Text(
                        text = notification.text!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Placeholder para el ícono de la aplicación
 */
@Composable
private fun AppIconPlaceholder(
    packageName: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getAppColor(packageName)
    val iconContentDescription = stringResource(R.string.cd_app_icon)
    
    Box(
        modifier = modifier
            .size(40.dp)
            .semantics { contentDescription = iconContentDescription },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = iconContentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Genera un color distintivo para cada paquete de aplicación
 */
@Composable
private fun getAppColor(packageName: String): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF45B7D1),
        Color(0xFF96CEB4),
        Color(0xFFFFEAA7),
        Color(0xFFDDA0DD),
        Color(0xFF98D8C8)
    )
    
    val hash = packageName.hashCode()
    val index = Math.abs(hash) % colors.size
    return colors[index]
}

/**
 * Convierte el package name en un nombre más amigable
 */
private fun getAppDisplayName(packageName: String): String {
    return when {
        packageName.contains("whatsapp") -> "WhatsApp"
        packageName.contains("telegram") -> "Telegram"
        packageName.contains("gmail") -> "Gmail"
        packageName.contains("chrome") -> "Chrome"
        packageName.contains("facebook") -> "Facebook"
        packageName.contains("instagram") -> "Instagram"
        packageName.contains("youtube") -> "YouTube"
        packageName.contains("spotify") -> "Spotify"
        packageName.contains("twitter") -> "Twitter"
        packageName.contains("linkedin") -> "LinkedIn"
        else -> {
            // Intentar extraer el nombre del paquete
            val parts = packageName.split(".")
            parts.lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
        }
    }
}

/**
 * Pantalla de carga
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.notification_history_loading),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }
    }
}

/**
 * Estado vacío cuando no hay notificaciones
 */
@Composable
private fun EmptyStateContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.notification_history_empty_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.notification_history_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Pantalla de error
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.notification_history_error_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.TextButton(
                onClick = onRetry
            ) {
                Text(stringResource(R.string.notification_history_retry))
            }
        }
    }
}

/**
 * Preview para desarrollo
 */
@Preview(showBackground = true)
@Composable
private fun NotificationHistoryScreenPreview() {
    NoteCoupleTakerTheme {
        NotificationHistoryScreen(
            onNavigateBack = { }
        )
    }
}
