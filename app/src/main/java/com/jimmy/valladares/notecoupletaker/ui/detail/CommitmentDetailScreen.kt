package com.jimmy.valladares.notecoupletaker.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jimmy.valladares.notecoupletaker.domain.model.ChecklistItem
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
import com.jimmy.valladares.notecoupletaker.ui.theme.ProgressGreen
import com.jimmy.valladares.notecoupletaker.ui.theme.ProgressGreenDark
import com.jimmy.valladares.notecoupletaker.ui.theme.QualityTimeTint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de detalle que muestra información completa de un compromiso y su checklist
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentDetailScreen(
    commitmentId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backButtonContentDescription = stringResource(R.string.cd_back_button)

    // Cargar el compromiso cuando se abre la pantalla
    LaunchedEffect(commitmentId) {
        viewModel.loadCommitment(commitmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.commitmentWithChecklist?.commitment?.title ?: "",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorScreen(
                    error = uiState.error!!,
                    onNavigateBack = onNavigateBack
                )
            }
            uiState.commitmentWithChecklist != null -> {
                DetailContent(
                    commitmentWithChecklist = uiState.commitmentWithChecklist!!,
                    progress = uiState.progress,
                    onChecklistItemToggle = { checklistItemId ->
                        viewModel.toggleChecklistItem(checklistItemId)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
/**
 * Contenido principal de la pantalla de detalle
 */
@Composable
private fun DetailContent(
    commitmentWithChecklist: CommitmentWithChecklist,
    progress: Float,
    onChecklistItemToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Información del compromiso
            CommitmentInfoCard(commitment = commitmentWithChecklist.commitment)
        }
        
        item {
            // Progreso del checklist con barra animada
            ProgressCard(progress = progress, checklist = commitmentWithChecklist.checklist)
        }
        
        item {
            // Título del checklist
            Text(
                text = stringResource(R.string.detail_checklist_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Items del checklist
        items(commitmentWithChecklist.checklist) { checklistItem ->
            ChecklistItemCard(
                item = checklistItem,
                onCheckedChange = { 
                    onChecklistItemToggle(checklistItem.id)
                }
            )
        }
        
        item {
            // Spacer final
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Contenido de carga
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
                text = "Cargando...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }
    }
}

/**
 * Tarjeta con la información principal del compromiso
 */
@Composable
private fun CommitmentInfoCard(commitment: Commitment) {
    val categoryColor = getCategoryColor(commitment.category)
    val formattedDate = formatCreationDate(commitment.creationDate)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Indicador de categoría
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = commitment.category.iconRes,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = getCategoryDisplayName(commitment.category),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // Descripción
            Text(
                text = commitment.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fecha de creación
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_created_on),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

/**
 * Tarjeta que muestra el progreso del checklist con animación
 */
@Composable
private fun ProgressCard(progress: Float, checklist: List<ChecklistItem>) {
    val completedItems = checklist.count { it.isChecked }
    val totalItems = checklist.size
    val isDarkTheme = isSystemInDarkTheme()
    val progressColor = if (isDarkTheme) ProgressGreenDark else ProgressGreen
    val progressBarContentDescription = stringResource(R.string.cd_progress_bar)
    
    // Animación del progreso
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "progress_animation"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_progress_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Text(
                    text = stringResource(R.string.detail_progress_format, completedItems, totalItems),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = progressColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .semantics { contentDescription = progressBarContentDescription },
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Porcentaje
            Text(
                text = "${(progress * 100).toInt()}% completado",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Tarjeta individual para cada ítem del checklist
 */
@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    onCheckedChange: (Boolean) -> Unit
) {
    val checklistItemContentDescription = stringResource(R.string.cd_checklist_item)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.semantics { 
                    contentDescription = checklistItemContentDescription 
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (item.isChecked) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Pantalla de error cuando no se encuentra el compromiso o hay un error de carga
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(
    error: String? = null,
    onNavigateBack: () -> Unit
) {
    val backButtonContentDescription = stringResource(R.string.cd_back_button)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Error") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backButtonContentDescription
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: stringResource(R.string.detail_error_commitment_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Obtiene el color de fondo para cada categoría (compatible con tema oscuro)
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
 * Formatea la fecha de creación de manera legible
 */
private fun formatCreationDate(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return dateTime.format(formatter)
}

/**
 * Preview para la pantalla de detalle
 */
@Preview(showBackground = true)
@Composable
private fun CommitmentDetailScreenPreview() {
    NoteCoupleTakerTheme {
        Box {
            Text("Preview no disponible - requiere ViewModel")
        }
    }
}
