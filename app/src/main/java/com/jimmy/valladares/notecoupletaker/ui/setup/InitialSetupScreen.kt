package com.jimmy.valladares.notecoupletaker.ui.setup

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jimmy.valladares.notecoupletaker.R
import com.jimmy.valladares.notecoupletaker.ui.theme.NoteCoupleTakerTheme
import com.jimmy.valladares.notecoupletaker.utils.NotificationPermissionUtils
import com.jimmy.valladares.notecoupletaker.utils.DeviceOptimizationUtils
import com.jimmy.valladares.notecoupletaker.utils.SetupPreferencesUtils

/**
 * Pantalla de configuración inicial que guía al usuario a través del proceso
 * de configuración de permisos necesarios para el funcionamiento de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Estados para rastrear el progreso de permisos
    var hasNotificationPermission by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationPermissionUtils.hasNotificationPermission(context)
            } else true
        )
    }
    var hasNotificationListenerAccess by remember { 
        mutableStateOf(NotificationPermissionUtils.isNotificationListenerEnabled(context)) 
    }
    var hasExactAlarmPermission by remember {
        mutableStateOf(NotificationPermissionUtils.hasExactAlarmPermission(context))
    }
    var hasBatteryOptimizationDisabled by remember {
        mutableStateOf(!NotificationPermissionUtils.isBatteryOptimizationEnabled(context))
    }
    
    var currentStep by remember { mutableStateOf(0) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    
    // Launcher para solicitar permisos de notificaciones (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            currentStep++
        }
    }
    
    // Launcher para configuraciones del sistema
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { 
        // Se verificará el estado cuando la actividad regrese
    }
    
    // Observar cambios de lifecycle para actualizar estados
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationListenerAccess = NotificationPermissionUtils.isNotificationListenerEnabled(context)
                hasExactAlarmPermission = NotificationPermissionUtils.hasExactAlarmPermission(context)
                hasBatteryOptimizationDisabled = !NotificationPermissionUtils.isBatteryOptimizationEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Verificar si la configuración está completa
    val allPermissionsGranted = hasNotificationPermission && 
                               hasNotificationListenerAccess && 
                               hasExactAlarmPermission && 
                               hasBatteryOptimizationDisabled
    
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
            // Iniciar servicios necesarios
            NotificationPermissionUtils.startKeepAliveServiceIfNeeded(context)
            // Marcar configuración como completada
            SetupPreferencesUtils.setInitialSetupCompleted(context)
            SetupPreferencesUtils.setFirstLaunchCompleted(context)
            onSetupComplete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración Inicial",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Encabezado de bienvenida
            WelcomeHeader()
            
            // Paso 1: Permisos de notificación (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionStepCard(
                    stepNumber = 1,
                    title = "Permisos de Notificación",
                    description = "Permite a la aplicación mostrar notificaciones para recordatorios",
                    icon = Icons.Default.Notifications,
                    isCompleted = hasNotificationPermission,
                    onActionClick = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }
            
            // Paso 2: Acceso a notificaciones del sistema
            PermissionStepCard(
                stepNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 2 else 1,
                title = "Captura de Recordatorios",
                description = "Permite crear recordatorios automáticamente cuando recibes mensajes importantes. Solo se analizan las notificaciones para detectar compromisos y citas.",
                icon = Icons.Default.Notifications,
                isCompleted = hasNotificationListenerAccess,
                onActionClick = {
                    showNotificationDialog = true
                }
            )
            
            // Paso 3: Permisos de alarma exacta
            PermissionStepCard(
                stepNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 3 else 2,
                title = "Alarmas Exactas",
                description = "Permite programar recordatorios en horarios exactos",
                icon = Icons.Default.Settings,
                isCompleted = hasExactAlarmPermission,
                onActionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        settingsLauncher.launch(intent)
                    }
                }
            )
            
            // Paso 4: Optimización de batería
            PermissionStepCard(
                stepNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 4 else 3,
                title = "Optimización de Batería",
                description = "Desactiva la optimización de batería para que la aplicación funcione en segundo plano",
                icon = Icons.Default.Settings,
                isCompleted = hasBatteryOptimizationDisabled,
                onActionClick = {
                    NotificationPermissionUtils.requestDisableBatteryOptimization(context)
                }
            )
            
            // Paso 5: Configuración específica del dispositivo
            if (DeviceOptimizationUtils.hasAggressiveBatteryOptimization()) {
                DeviceSpecificOptimizationCard(
                    stepNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 5 else 4
                )
            }
            
            // Botón para completar configuración (solo si todos los permisos están dados)
            if (allPermissionsGranted) {
                Button(
                    onClick = onSetupComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Completar Configuración",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
    
    // Diálogo explicativo para el permiso de notificaciones
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Text(
                    text = "Permiso de Notificaciones",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Esta aplicación necesita acceso a las notificaciones para:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "• Detectar mensajes con fechas y compromisos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Crear recordatorios automáticamente",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Ayudarte a no olvidar citas importantes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "⚠️ Android mostrará una advertencia de seguridad. Esto es normal y seguro para esta aplicación.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationDialog = false
                        NotificationPermissionUtils.openNotificationListenerSettings(context)
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNotificationDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun WelcomeHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "¡Bienvenido a NoteCoupleTaker!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Para funcionar correctamente, necesitamos configurar algunos permisos. Este proceso solo se hace una vez.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PermissionStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCompleted: Boolean,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Número de paso
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Ícono del permiso
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Título
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Descripción
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de acción
            if (!isCompleted) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar")
                }
            } else {
                OutlinedButton(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurado")
                }
            }
        }
    }
}

@Composable
private fun DeviceSpecificOptimizationCard(stepNumber: Int) {
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.error
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stepNumber.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Configuración ${DeviceOptimizationUtils.getManufacturerName()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Tu dispositivo ${DeviceOptimizationUtils.getManufacturerName()} requiere configuración adicional para evitar que termine la aplicación en segundo plano.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showInstructions = !showInstructions },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showInstructions) "Ocultar Guía" else "Ver Guía")
                }
                
                Button(
                    onClick = { 
                        DeviceOptimizationUtils.openBatteryOptimizationSettings(context)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Configurar")
                }
            }
            
            if (showInstructions) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Pasos para ${DeviceOptimizationUtils.getManufacturerName()}:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        DeviceOptimizationUtils.getOptimizationInstructions().forEachIndexed { index, instruction ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = instruction,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialSetupScreenPreview() {
    NoteCoupleTakerTheme {
        InitialSetupScreen(
            onSetupComplete = {}
        )
    }
}
