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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteCoupleTakerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Manejar navegación desde notificaciones
                    LaunchedEffect(Unit) {
                        val commitmentId = intent?.getIntExtra("commitmentId", -1)
                        if (commitmentId != null && commitmentId != -1) {
                            navController.navigate("${NoteCoupleTakerDestinations.COMMITMENT_DETAIL_ROUTE}/$commitmentId")
                        }
                    }
                    
                    NoteCoupleTakerNavHost(navController = navController)
                }
            }
        }
    }
}