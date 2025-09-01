package com.jimmy.valladares.notecoupletaker.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimmy.valladares.notecoupletaker.R
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentWithChecklist
import com.jimmy.valladares.notecoupletaker.ui.theme.CommunicationTint
import com.jimmy.valladares.notecoupletaker.ui.theme.DarkCommunicationTint
import com.jimmy.valladares.notecoupletaker.ui.theme.DarkGoalsTint
import com.jimmy.valladares.notecoupletaker.ui.theme.DarkHabitsTint
import com.jimmy.valladares.notecoupletaker.ui.theme.DarkPersonalGrowthTint
import com.jimmy.valladares.notecoupletaker.ui.theme.DarkQualityTimeTint
import com.jimmy.valladares.notecoupletaker.ui.theme.GoalsTint
import com.jimmy.valladares.notecoupletaker.ui.theme.HabitsTint
import com.jimmy.valladares.notecoupletaker.ui.theme.NoteCoupleTakerTheme
import com.jimmy.valladares.notecoupletaker.ui.theme.PersonalGrowthTint
import com.jimmy.valladares.notecoupletaker.ui.theme.QualityTimeTint
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Pantalla principal que muestra la lista de compromisos de la pareja
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onAddCommitmentClick: () -> Unit = {},
    onCommitmentClick: (Int) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val addCommitmentContentDescription = stringResource(R.string.cd_add_commitment)
    val settingsContentDescription = stringResource(R.string.cd_settings_button)
    val deleteSuccessMessage = stringResource(R.string.delete_success_message)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para el diálogo de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commitmentToDelete by remember { mutableStateOf<CommitmentWithChecklist?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.semantics { 
                            contentDescription = settingsContentDescription 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = settingsContentDescription
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCommitmentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics { 
                    contentDescription = addCommitmentContentDescription 
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = addCommitmentContentDescription
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header de la pantalla
            HomeHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Contenido basado en el estado
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(error = uiState.error!!)
                }
                uiState.commitments.isEmpty() -> {
                    EmptyStateContent()
                }
                else -> {
                    CommitmentsList(
                        commitments = uiState.commitments,
                        onCommitmentClick = onCommitmentClick,
                        onDeleteRequest = { commitment ->
                            commitmentToDelete = commitment
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && commitmentToDelete != null) {
            DeleteConfirmationDialog(
                commitmentTitle = commitmentToDelete!!.commitment.title,
                onConfirm = {
                    viewModel.deleteCommitment(commitmentToDelete!!.commitment.id)
                    showDeleteDialog = false
                    commitmentToDelete = null
                    
                    // Mostrar snackbar de confirmación
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = deleteSuccessMessage
                        )
                    }
                },
                onDismiss = {
                    showDeleteDialog = false
                    commitmentToDelete = null
                }
            )
        }
    }
}

/**
 * Header con título y subtítulo motivador
 */
@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Lista de compromisos usando LazyColumn para optimización
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommitmentsList(
    commitments: List<CommitmentWithChecklist>,
    onCommitmentClick: (Int) -> Unit,
    onDeleteRequest: (CommitmentWithChecklist) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = commitments,
            key = { it.commitment.id }
        ) { commitmentWithChecklist ->
            SwipeToDeleteItem(
                commitmentWithChecklist = commitmentWithChecklist,
                onCommitmentClick = { onCommitmentClick(commitmentWithChecklist.commitment.id) },
                onDeleteRequest = { onDeleteRequest(commitmentWithChecklist) }
            )
        }
    }
}

/**
 * Item con funcionalidad de swipe to delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    commitmentWithChecklist: CommitmentWithChecklist,
    onCommitmentClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteRequest()
                    false // No eliminamos inmediatamente, esperamos confirmación
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            DeleteBackground(swipeState.targetValue)
        },
        content = {
            CommitmentCard(
                commitmentWithChecklist = commitmentWithChecklist,
                onClick = onCommitmentClick
            )
        }
    )
}

/**
 * Fondo que se muestra durante el swipe para indicar eliminación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(dismissValue: SwipeToDismissBoxValue) {
    val color by animateColorAsState(
        when (dismissValue) {
            SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
            SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
        },
        label = "delete_background_color"
    )
    
    val deleteBackgroundContentDescription = stringResource(R.string.cd_delete_background)
    val deleteIconContentDescription = stringResource(R.string.cd_delete_icon)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp)
            .semantics { contentDescription = deleteBackgroundContentDescription },
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = deleteIconContentDescription,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Diálogo de confirmación para eliminar compromiso
 */
@Composable
private fun DeleteConfirmationDialog(
    commitmentTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_dialog_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.delete_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$commitmentTitle\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(R.string.delete_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.delete_dialog_cancel),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Tarjeta individual para cada compromiso
 */
@Composable
private fun CommitmentCard(
    commitmentWithChecklist: CommitmentWithChecklist,
    onClick: () -> Unit
) {
    val commitment = commitmentWithChecklist.commitment
    val categoryColor = getCategoryColor(commitment.category)
    val cardContentDescription = stringResource(R.string.cd_commitment_card)
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardContentDescription },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de categoría con ícono
            CategoryIndicator(
                category = commitment.category,
                backgroundColor = categoryColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido del compromiso
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = commitment.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = getCategoryDisplayName(commitment.category),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Indicador visual de la categoría con ícono
 */
@Composable
private fun CategoryIndicator(
    category: CommitmentCategory,
    backgroundColor: Color
) {
    val iconContentDescription = stringResource(R.string.cd_category_icon)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .semantics { contentDescription = iconContentDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.iconRes,
            fontSize = 20.sp
        )
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
                text = stringResource(R.string.home_loading),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }
    }
}

/**
 * Estado vacío cuando no hay compromisos
 */
@Composable
private fun EmptyStateContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_empty_state),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Pantalla de error
 */
@Composable
private fun ErrorContent(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_error_message),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.error
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Obtiene el color de fondo para cada categoría
 */
@Composable
private fun getCategoryColor(category: CommitmentCategory): Color {
    val isDarkTheme = isSystemInDarkTheme()
    return when (category) {
        CommitmentCategory.COMMUNICATION -> if (isDarkTheme) DarkCommunicationTint else CommunicationTint
        CommitmentCategory.HABITS -> if (isDarkTheme) DarkHabitsTint else HabitsTint
        CommitmentCategory.GOALS -> if (isDarkTheme) DarkGoalsTint else GoalsTint
        CommitmentCategory.QUALITY_TIME -> if (isDarkTheme) DarkQualityTimeTint else QualityTimeTint
        CommitmentCategory.PERSONAL_GROWTH -> if (isDarkTheme) DarkPersonalGrowthTint else PersonalGrowthTint
    }
}

/**
 * Obtiene el nombre de la categoría localizado
 */
@Composable
private fun getCategoryDisplayName(category: CommitmentCategory): String {
    return when (category) {
        CommitmentCategory.COMMUNICATION -> stringResource(R.string.category_communication)
        CommitmentCategory.HABITS -> stringResource(R.string.category_habits)
        CommitmentCategory.GOALS -> stringResource(R.string.category_goals)
        CommitmentCategory.QUALITY_TIME -> stringResource(R.string.category_quality_time)
        CommitmentCategory.PERSONAL_GROWTH -> stringResource(R.string.category_personal_growth)
    }
}

/**
 * Preview para la pantalla principal
 */
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NoteCoupleTakerTheme {
        HomeScreen()
    }
}
