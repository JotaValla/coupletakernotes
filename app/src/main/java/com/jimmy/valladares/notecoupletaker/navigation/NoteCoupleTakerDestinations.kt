package com.jimmy.valladares.notecoupletaker.navigation

/**
 * Definición de rutas para la navegación de la aplicación
 */
object NoteCoupleTakerDestinations {
    const val INITIAL_SETUP_ROUTE = "initial_setup"
    const val HOME_ROUTE = "home"
    const val ADD_COMMITMENT_ROUTE = "add_commitment"
    const val COMMITMENT_DETAIL_ROUTE = "commitment_detail"
    const val SETTINGS_ROUTE = "settings"
    const val NOTIFICATION_HISTORY_ROUTE = "notification_history"
    
    // Argumentos de navegación
    const val COMMITMENT_ID_ARG = "commitmentId"
    
    // Rutas con argumentos
    const val COMMITMENT_DETAIL_ROUTE_WITH_ARGS = "$COMMITMENT_DETAIL_ROUTE/{$COMMITMENT_ID_ARG}"
}
