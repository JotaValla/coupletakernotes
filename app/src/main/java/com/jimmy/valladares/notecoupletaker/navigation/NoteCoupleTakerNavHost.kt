package com.jimmy.valladares.notecoupletaker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jimmy.valladares.notecoupletaker.ui.addcommitment.AddCommitmentScreen
import com.jimmy.valladares.notecoupletaker.ui.detail.CommitmentDetailScreen
import com.jimmy.valladares.notecoupletaker.ui.home.HomeScreen
import com.jimmy.valladares.notecoupletaker.ui.home.HomeViewModel
import com.jimmy.valladares.notecoupletaker.ui.notifications.NotificationHistoryScreen
import com.jimmy.valladares.notecoupletaker.ui.settings.SettingsScreen
import com.jimmy.valladares.notecoupletaker.ui.setup.InitialSetupScreen

/**
 * NavHost principal que maneja la navegación entre pantallas
 */
@Composable
fun NoteCoupleTakerNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    // ViewModel compartido entre pantallas para manejar el estado de los compromisos
    val homeViewModel: HomeViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pantalla de configuración inicial
        composable(NoteCoupleTakerDestinations.INITIAL_SETUP_ROUTE) {
            InitialSetupScreen(
                onSetupComplete = {
                    // Una vez completada la configuración, navegar al home
                    navController.navigate(NoteCoupleTakerDestinations.HOME_ROUTE) {
                        // Limpiar la pila de navegación para que no se pueda regresar
                        popUpTo(NoteCoupleTakerDestinations.INITIAL_SETUP_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(NoteCoupleTakerDestinations.HOME_ROUTE) {
            HomeScreen(
                viewModel = homeViewModel,
                onAddCommitmentClick = {
                    navController.navigate(NoteCoupleTakerDestinations.ADD_COMMITMENT_ROUTE)
                },
                onCommitmentClick = { commitmentId ->
                    navController.navigate("${NoteCoupleTakerDestinations.COMMITMENT_DETAIL_ROUTE}/$commitmentId")
                },
                onSettingsClick = {
                    navController.navigate(NoteCoupleTakerDestinations.SETTINGS_ROUTE)
                }
            )
        }
        
        composable(NoteCoupleTakerDestinations.ADD_COMMITMENT_ROUTE) {
            AddCommitmentScreen(
                viewModel = homeViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NoteCoupleTakerDestinations.COMMITMENT_DETAIL_ROUTE_WITH_ARGS) { backStackEntry ->
            val commitmentIdString = backStackEntry.arguments?.getString(NoteCoupleTakerDestinations.COMMITMENT_ID_ARG)
            val commitmentId = commitmentIdString?.toIntOrNull()
            
            if (commitmentId != null) {
                CommitmentDetailScreen(
                    commitmentId = commitmentId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(NoteCoupleTakerDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NoteCoupleTakerDestinations.NOTIFICATION_HISTORY_ROUTE) {
            NotificationHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
