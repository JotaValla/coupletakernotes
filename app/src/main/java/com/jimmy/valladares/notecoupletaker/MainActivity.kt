package com.jimmy.valladares.notecoupletaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jimmy.valladares.notecoupletaker.navigation.NoteCoupleTakerDestinations
import com.jimmy.valladares.notecoupletaker.navigation.NoteCoupleTakerNavHost
import com.jimmy.valladares.notecoupletaker.ui.theme.NoteCoupleTakerTheme
import com.jimmy.valladares.notecoupletaker.utils.NotificationPermissionUtils
import com.jimmy.valladares.notecoupletaker.utils.SetupPreferencesUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Iniciar el servicio KeepAlive si ya tenemos permisos de notificación
        NotificationPermissionUtils.startKeepAliveServiceIfNeeded(this)
        
        setContent {
            NoteCoupleTakerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Determinar la pantalla de inicio basándose en si es la primera vez
                    val startDestination = if (SetupPreferencesUtils.shouldShowInitialSetup(this@MainActivity)) {
                        NoteCoupleTakerDestinations.INITIAL_SETUP_ROUTE
                    } else {
                        NoteCoupleTakerDestinations.HOME_ROUTE
                    }
                    
                    // Manejar navegación desde notificaciones
                    LaunchedEffect(Unit) {
                        val commitmentId = intent?.getIntExtra("commitmentId", -1)
                        if (commitmentId != null && commitmentId != -1 && startDestination == NoteCoupleTakerDestinations.HOME_ROUTE) {
                            // Solo navegar al detalle si no estamos en configuración inicial
                            navController.navigate("${NoteCoupleTakerDestinations.COMMITMENT_DETAIL_ROUTE}/$commitmentId")
                        }
                    }
                    
                    NoteCoupleTakerNavHost(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}