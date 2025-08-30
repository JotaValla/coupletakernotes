package com.jimmy.valladares.notecoupletaker.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import com.jimmy.valladares.notecoupletaker.R
import com.jimmy.valladares.notecoupletaker.domain.model.ChecklistItem
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import com.jimmy.valladares.notecoupletaker.ui.home.HomeViewModel
import com.jimmy.valladares.notecoupletaker.ui.theme.CommunicationTint
import com.jimmy.valladares.notecoupletaker.ui.theme.GoalsTint
import com.jimmy.valladares.notecoupletaker.ui.theme.HabitsTint
import com.jimmy.valladares.notecoupletaker.ui.theme.NoteCoupleTakerTheme
import com.jimmy.valladares.notecoupletaker.ui.theme.PersonalGrowthTint
import com.jimmy.valladares.notecoupletaker.ui.theme.QualityTimeTint

/**
 * Pantalla de detalle que muestra información completa de un compromiso y su checklist
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentDetailScreen(
    commitmentId: String,
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val commitment = uiState.commitments.find { it.id == commitmentId }
    val backButtonContentDescription = stringResource(R.string.cd_back_button)

    if (commitment == null) {
        // Mostrar pantalla de error si no se encuentra el compromiso
        ErrorScreen(onNavigateBack = onNavigateBack)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = commitment.title,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Información del compromiso
                CommitmentInfoCard(commitment = commitment)
            }
            
            item {
                // Progreso del checklist
                ChecklistProgressCard(commitment = commitment)
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
            items(commitment.checklist) { checklistItem ->
                ChecklistItemCard(
                    item = checklistItem,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleChecklistItem(
                            commitmentId = commitment.id,
                            checklistItemId = checklistItem.id
                        )
                    }
                )
            }
            
            item {
                // Spacer final
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Tarjeta con la información principal del compromiso
 */
@Composable
private fun CommitmentInfoCard(commitment: Commitment) {
    val categoryColor = getCategoryColor(commitment.category)
    
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
        }
    }
}

/**
 * Tarjeta que muestra el progreso del checklist
 */
@Composable
private fun ChecklistProgressCard(commitment: Commitment) {
    val completedItems = commitment.checklist.count { it.isChecked }
    val totalItems = commitment.checklist.size
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    
    // Animación del progreso
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
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
                    text = "Progreso",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Text(
                    text = stringResource(R.string.detail_progress_format, completedItems, totalItems),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
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
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
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
 * Pantalla de error cuando no se encuentra el compromiso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(onNavigateBack: () -> Unit) {
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
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.detail_error_commitment_not_found),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Obtiene el color de fondo para cada categoría
 */
@Composable
private fun getCategoryColor(category: CommitmentCategory): Color {
    return when (category) {
        CommitmentCategory.COMMUNICATION -> CommunicationTint
        CommitmentCategory.HABITS -> HabitsTint
        CommitmentCategory.GOALS -> GoalsTint
        CommitmentCategory.QUALITY_TIME -> QualityTimeTint
        CommitmentCategory.PERSONAL_GROWTH -> PersonalGrowthTint
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
