package com.jimmy.valladares.notecoupletaker.ui.addcommitment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jimmy.valladares.notecoupletaker.R
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import com.jimmy.valladares.notecoupletaker.ui.home.HomeViewModel
import com.jimmy.valladares.notecoupletaker.ui.theme.NoteCoupleTakerTheme

/**
 * Pantalla para agregar un nuevo compromiso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommitmentScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    // Estados locales para los campos del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CommitmentCategory?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val backButtonContentDescription = stringResource(R.string.cd_back_button)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_commitment_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Formulario en una tarjeta
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo de título
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = {
                            Text(text = stringResource(R.string.add_commitment_title_label))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.add_commitment_title_placeholder))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Campo de descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = {
                            Text(text = stringResource(R.string.add_commitment_description_label))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.add_commitment_description_placeholder))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Dropdown para categoría
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.let { getCategoryDisplayName(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(text = stringResource(R.string.add_commitment_category_label))
                            },
                            placeholder = {
                                Text(text = stringResource(R.string.add_commitment_category_placeholder))
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CommitmentCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = category.iconRes,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(text = getCategoryDisplayName(category))
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Botón de guardar
            Button(
                onClick = {
                    // Crear el nuevo compromiso
                    selectedCategory?.let { category ->
                        val newCommitment = Commitment(
                            title = title.trim(),
                            description = description.trim(),
                            category = category
                        )
                        
                        // Agregar el compromiso usando el ViewModel
                        viewModel.addCommitment(newCommitment)
                        
                        // Navegar de regreso
                        onNavigateBack()
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank() && selectedCategory != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.add_commitment_save_button),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Spacer para que el contenido no quede muy pegado al final
            Spacer(modifier = Modifier.height(16.dp))
        }
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
 * Preview para la pantalla de agregar compromiso
 */
@Preview(showBackground = true)
@Composable
private fun AddCommitmentScreenPreview() {
    NoteCoupleTakerTheme {
        Box {
            // No podemos crear un ViewModel en el preview, así que usamos un placeholder
            Text("Preview no disponible - requiere ViewModel")
        }
    }
}
