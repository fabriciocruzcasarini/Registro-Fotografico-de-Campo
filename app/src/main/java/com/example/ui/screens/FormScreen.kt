package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PhotoCaptureCard
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.FieldActivityViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: FieldActivityViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val photoCount by viewModel.photoCount.collectAsState()

    // Fetch initial GPS coordinates on load and clean orphan temp files
    LaunchedEffect(Unit) {
        viewModel.fetchCurrentLocation(context)
        viewModel.cleanupAllOrphanTempFiles(context)
    }

    var expandedActivity by remember { mutableStateOf(false) }
    var expandedStudType by remember { mutableStateOf(false) }
    var expandedPlateType by remember { mutableStateOf(false) }
    var expandedDirection by remember { mutableStateOf(false) }
    var expandedLane by remember { mutableStateOf(false) }
    var expandedLaneSelect by remember { mutableStateOf(false) }

    // Coordinates section collapsed by default as requested
    var isCoordinatesExpanded by remember { mutableStateOf(false) }

    // State for Lane Info Dialog (?)
    var showLaneInfoDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner: Modo Edição ou Novo Registro
        if (formState.isEditing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF3C7) // Amber/Yellow warm background
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Editando Registro #${formState.editingActivityId}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Altere os dados ou fotos e salve/envie as atualizações.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.cancelEditing() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF92400E)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar Edição",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Cancelar", fontSize = 11.sp)
                    }
                }
            }
        } else {
            // Top Banner / Quick Fill bar for testing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Novo Registro em Campo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Preencha os dados e tire as fotos do Antes e Depois",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resetFormForNewRecord() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Limpar / Novo",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Limpar", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.fillSampleData(context) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Preencher Exemplo",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Exemplo", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        if (formState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = formState.errorMessage!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Section 1: Formulário Principal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. Dados da Operação",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Field 1: Nome do operador
                OutlinedTextField(
                    value = formState.operatorName,
                    onValueChange = { viewModel.updateOperatorName(it) },
                    label = { Text("Nome do Encarregado *") },
                    placeholder = { Text("Ex: Carlos Eduardo") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Field 2: Atividade (Dropdown Select)
                val isPlaceholderActivity = formState.activityType == FieldActivityViewModel.DEFAULT_ACTIVITY_PLACEHOLDER
                ExposedDropdownMenuBox(
                    expanded = expandedActivity,
                    onExpandedChange = { expandedActivity = !expandedActivity },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formState.activityType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Atividade *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = if (isPlaceholderActivity) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActivity) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            unfocusedTextColor = if (isPlaceholderActivity) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = if (isPlaceholderActivity) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedActivity,
                        onDismissRequest = { expandedActivity = false }
                    ) {
                        viewModel.activityOptions.forEach { option ->
                            val isPlaceholder = option == FieldActivityViewModel.DEFAULT_ACTIVITY_PLACEHOLDER
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    if (!isPlaceholder) {
                                        viewModel.updateActivityType(option)
                                    }
                                    expandedActivity = false
                                }
                            )
                        }
                    }
                }

                // Field 2.1: Tipo de Tacha (Condicional: Ativo e visível quando Atividade for Implantação Tacha)
                if (formState.activityType == "Implantação Tacha") {
                    val isPlaceholderStud = formState.studType == FieldActivityViewModel.DEFAULT_STUD_PLACEHOLDER
                    ExposedDropdownMenuBox(
                        expanded = expandedStudType,
                        onExpandedChange = { expandedStudType = !expandedStudType },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = formState.studType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Tacha *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = if (isPlaceholderStud) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudType) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                unfocusedTextColor = if (isPlaceholderStud) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                                focusedTextColor = if (isPlaceholderStud) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedStudType,
                            onDismissRequest = { expandedStudType = false }
                        ) {
                            viewModel.studTypeOptions.forEach { option ->
                                val isPlaceholder = option == FieldActivityViewModel.DEFAULT_STUD_PLACEHOLDER
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        if (!isPlaceholder) {
                                            viewModel.updateStudType(option)
                                        }
                                        expandedStudType = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Field 3: Rodovia
                OutlinedTextField(
                    value = formState.highway,
                    onValueChange = { viewModel.updateHighway(it) },
                    label = { Text("Rodovia *") },
                    placeholder = { Text("Ex: BR-101, SP-330") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Field 4 & 5: Sentido e Pista (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sentido
                    ExposedDropdownMenuBox(
                        expanded = expandedDirection,
                        onExpandedChange = { expandedDirection = !expandedDirection },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = formState.direction,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sentido *", maxLines = 1, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDirection) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDirection,
                            onDismissRequest = { expandedDirection = false }
                        ) {
                            viewModel.directionOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.updateDirection(option)
                                        expandedDirection = false
                                    }
                                )
                            }
                        }
                    }

                    // Pista
                    ExposedDropdownMenuBox(
                        expanded = expandedLane,
                        onExpandedChange = { expandedLane = !expandedLane },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = formState.laneType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pista *", maxLines = 1, fontSize = 13.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLane) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedLane,
                            onDismissRequest = { expandedLane = false }
                        ) {
                            viewModel.laneTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.updateLaneType(option)
                                        expandedLane = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Fields 6 & 7: KM Inicial e KM Final (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.kmStart,
                        onValueChange = { viewModel.updateKmStart(it) },
                        label = { Text("KM Inicial *", maxLines = 1, fontSize = 13.sp) },
                        placeholder = { Text("120.0") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = formState.kmEnd,
                        onValueChange = { viewModel.updateKmEnd(it) },
                        label = { Text("KM Final *", maxLines = 1, fontSize = 13.sp) },
                        placeholder = { Text("122.5") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // Fields 8 & 9: Faixa (Select Dinâmico) e Eixo (Row lado a lado)
                val dynamicLaneOptions = viewModel.getLaneOptions(formState.activityType)
                val isPlaceholderLane = formState.lane == FieldActivityViewModel.DEFAULT_LANE_PLACEHOLDER

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Faixa (Select Dinâmico baseado na Atividade)
                    ExposedDropdownMenuBox(
                        expanded = expandedLaneSelect,
                        onExpandedChange = { expandedLaneSelect = !expandedLaneSelect },
                        modifier = Modifier.weight(1.15f)
                    ) {
                        OutlinedTextField(
                            value = formState.lane,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Faixa", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(? Legendas)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLaneSelect)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                unfocusedTextColor = if (isPlaceholderLane) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                                focusedTextColor = if (isPlaceholderLane) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedLaneSelect,
                            onDismissRequest = { expandedLaneSelect = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ver Guia de Legendas das Faixas",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    expandedLaneSelect = false
                                    showLaneInfoDialog = true
                                }
                            )

                            dynamicLaneOptions.forEach { option ->
                                val isPlaceholder = option == FieldActivityViewModel.DEFAULT_LANE_PLACEHOLDER
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateLane(option)
                                        expandedLaneSelect = false
                                    }
                                )
                            }
                        }
                    }

                    // Eixo
                    OutlinedTextField(
                        value = formState.eixo,
                        onValueChange = { viewModel.updateEixo(it) },
                        label = { Text("Eixo", fontSize = 13.sp) },
                        placeholder = { Text("Ex: Central") },
                        modifier = Modifier.weight(0.85f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }

                // Campo Condicional: Descrição Legenda (Exibido quando Faixa for LEGENDA)
                if (formState.lane.equals("LEGENDA", ignoreCase = true)) {
                    OutlinedTextField(
                        value = formState.legendDescription,
                        onValueChange = { viewModel.updateLegendDescription(it) },
                        label = { Text("Descrição Legenda *") },
                        placeholder = { Text("Ex: PARE, DEVAGAR, ESCOLA, BUS...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }

                // Bloco Condicional: Campos de Placas (Ativo para Implantação e Remoção de Placas)
                if (viewModel.isPlateActivity(formState.activityType)) {
                    val isPlaceholderPlateType = formState.plateType == FieldActivityViewModel.DEFAULT_PLATE_TYPE_PLACEHOLDER

                    // Campo de seleção: Tipo de Placa
                    ExposedDropdownMenuBox(
                        expanded = expandedPlateType,
                        onExpandedChange = { expandedPlateType = !expandedPlateType },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = formState.plateType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Placa *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = if (isPlaceholderPlateType) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlateType) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                unfocusedTextColor = if (isPlaceholderPlateType) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                                focusedTextColor = if (isPlaceholderPlateType) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedPlateType,
                            onDismissRequest = { expandedPlateType = false }
                        ) {
                            viewModel.plateTypeOptions.forEach { option ->
                                val isPlaceholder = option == FieldActivityViewModel.DEFAULT_PLATE_TYPE_PLACEHOLDER
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        if (!isPlaceholder) {
                                            viewModel.updatePlateType(option)
                                        }
                                        expandedPlateType = false
                                    }
                                )
                            }
                        }
                    }

                    // Campo de Texto: Cód. Placa
                    OutlinedTextField(
                        value = formState.plateCode,
                        onValueChange = { viewModel.updatePlateCode(it) },
                        label = { Text("Cód. Placa") },
                        placeholder = { Text("Ex: R-1, A-1a, I-24...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = if (formState.plateType == "Indicação") ImeAction.Next else ImeAction.Done
                        )
                    )

                    // Se no campo Tipo de Placa for selecionada a opção "Indicação", adicionar um novo campo de texto abaixo do campo Cód. Placa com Label "Texto da Placa"
                    if (formState.plateType == "Indicação") {
                        OutlinedTextField(
                            value = formState.plateText,
                            onValueChange = { viewModel.updatePlateText(it) },
                            label = { Text("Texto da Placa *") },
                            placeholder = { Text("Ex: RETORNO A 500M, SÃO PAULO 45 KM...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }
                }

                // Field 10: Cadência
                OutlinedTextField(
                    value = formState.cadence,
                    onValueChange = { viewModel.updateCadence(it) },
                    label = { Text("Cadência") },
                    placeholder = { Text("Ex: 1:1, 1:3, 12m, Padrão") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Straighten, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Field 11: Observações
                OutlinedTextField(
                    value = formState.observations,
                    onValueChange = { viewModel.updateObservations(it) },
                    label = { Text("Observações") },
                    placeholder = { Text("Observações ou detalhes adicionais sobre o serviço...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )
            }
        }

        // Section 2: Coordenadas do Local (Escondido / Colapsível por padrão)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCoordinatesExpanded = !isCoordinatesExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "2. Coordenadas do Local",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            GpsAccuracyBadge(
                                accuracy = formState.gpsAccuracy,
                                isFetching = formState.isFetchingLocation
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.fetchCurrentLocation(context) }
                        ) {
                            if (formState.isFetchingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Atualizar GPS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = { isCoordinatesExpanded = !isCoordinatesExpanded }) {
                            Icon(
                                imageVector = if (isCoordinatesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isCoordinatesExpanded) "Recolher" else "Expandir",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isCoordinatesExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // GPS Sensor Telemetry & Quality Meter Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SignalCellularAlt,
                                            contentDescription = null,
                                            tint = when {
                                                formState.gpsAccuracy == null -> MaterialTheme.colorScheme.primary
                                                formState.gpsAccuracy!! <= 10f -> Color(0xFF2E7D32)
                                                formState.gpsAccuracy!! <= 25f -> Color(0xFFF57F17)
                                                else -> Color(0xFFD32F2F)
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Precisão do Sensor GPS",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = if (formState.gpsAccuracy != null) {
                                            String.format(Locale.getDefault(), "± %.1f metros", formState.gpsAccuracy)
                                        } else {
                                            formState.locationStatusText
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            formState.gpsAccuracy == null -> MaterialTheme.colorScheme.primary
                                            formState.gpsAccuracy!! <= 10f -> Color(0xFF2E7D32)
                                            formState.gpsAccuracy!! <= 25f -> Color(0xFFF57F17)
                                            else -> Color(0xFFD32F2F)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Visual Accuracy 3-Step Meter
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val acc = formState.gpsAccuracy
                                    val isHigh = acc != null && acc <= 10f
                                    val isMedium = acc != null && acc > 10f && acc <= 25f
                                    val isLow = acc != null && acc > 25f

                                    GpsLevelMeterItem(
                                        label = "Alta (≤10m)",
                                        isActive = isHigh,
                                        activeColor = Color(0xFF2E7D32),
                                        modifier = Modifier.weight(1f)
                                    )
                                    GpsLevelMeterItem(
                                        label = "Média (11-25m)",
                                        isActive = isMedium,
                                        activeColor = Color(0xFFF57F17),
                                        modifier = Modifier.weight(1f)
                                    )
                                    GpsLevelMeterItem(
                                        label = "Baixa (>25m)",
                                        isActive = isLow,
                                        activeColor = Color(0xFFD32F2F),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (formState.gpsAccuracy != null && formState.gpsAccuracy!! > 25f) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Aviso: Sinal fraco. Mantenha o celular desobstruído sob céu aberto por alguns segundos e toque em Atualizar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Campos capturados automaticamente via GPS (Marca d'água):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Non-editable Latitude & Longitude fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = String.format(Locale.US, "%.6f", formState.latitude),
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = String.format(Locale.US, "%.6f", formState.longitude),
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Registro Fotográfico (2 ou 3 fotos conforme Configurações)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "3. Registro Fotográfico ($photoCount Fotos)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (photoCount == 3) "Antes • Durante • Depois" else "Antes • Depois",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Photo Antes
        PhotoCaptureCard(
            title = "1. Foto ANTES (Execução)",
            subtitle = "Fotografe o local antes da realização do serviço",
            badgeColor = Color(0xFFEAB308), // Yellow
            selectedPhotoUri = formState.photoBeforeUri,
            onPhotoCaptured = { uri -> viewModel.setPhotoBefore(uri) },
            onPhotoRemoved = { viewModel.removePhotoBefore() }
        )

        // Photo Durante (Condicional para 3 fotos)
        if (photoCount == 3) {
            PhotoCaptureCard(
                title = "2. Foto DURANTE (Processo)",
                subtitle = "Fotografe a execução do serviço em andamento",
                badgeColor = Color(0xFF0284C7), // Sky/Blue
                selectedPhotoUri = formState.photoDuringUri,
                onPhotoCaptured = { uri -> viewModel.setPhotoDuring(uri) },
                onPhotoRemoved = { viewModel.removePhotoDuring() }
            )
        }

        // Photo Depois
        PhotoCaptureCard(
            title = if (photoCount == 3) "3. Foto DEPOIS (Conclusão)" else "2. Foto DEPOIS (Conclusão)",
            subtitle = "Fotografe o serviço concluído no mesmo ângulo",
            badgeColor = Color(0xFF22C55E), // Green
            selectedPhotoUri = formState.photoAfterUri,
            onPhotoCaptured = { uri -> viewModel.setPhotoAfter(uri) },
            onPhotoRemoved = { viewModel.removePhotoAfter() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Save & Share Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Action 1: Immediate WhatsApp Share / Atualizar e Enviar
            Button(
                onClick = {
                    viewModel.saveActivity(context, shareImmediatelyToWhatsApp = true)
                },
                enabled = !formState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerando Marca d'Água...", fontSize = 14.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = if (formState.isEditing) "Atualizar e Enviar WhatsApp" else "Salvar e Enviar WhatsApp",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (formState.isEditing) "Atualizar e Enviar no WhatsApp" else "Salvar e Enviar no WhatsApp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Action 2: Save locally / Atualizar no Histórico
            OutlinedButton(
                onClick = {
                    viewModel.saveActivity(context, shareImmediatelyToWhatsApp = false)
                },
                enabled = !formState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = if (formState.isEditing) "Salvar Alterações" else "Salvar para Envio Posterior",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.isEditing) "Salvar Alterações (Pendente)" else "Salvar para Envio Posterior",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Action 3: Se estiver em modo edição, botão para descartar alterações
            if (formState.isEditing) {
                TextButton(
                    onClick = { viewModel.cancelEditing() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar Edição",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancelar Edição e Limpar Formulário",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Dialog: Informações das Legendas das Faixas
    if (showLaneInfoDialog) {
        AlertDialog(
            onDismissRequest = { showLaneInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Legendas das Faixas",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Guia de siglas e nomenclaturas das faixas e marcas viárias:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LaneLegendItem("LBO-D", "Linha de Bordo - Direita")
                    LaneLegendItem("LBO-E", "Linha de Bordo - Esquerda")
                    LaneLegendItem("LMS-1", "Linha de Marcação Seccionada 1 (Divisão de Faixas)")
                    LaneLegendItem("LMS-2", "Linha de Marcação Seccionada 2")
                    LaneLegendItem("LFO-1", "Linha de Faixa de Operação 1 / Fracionada")
                    LaneLegendItem("LFO-2", "Linha de Faixa de Operação 2")
                    LaneLegendItem("LFO-3", "Linha de Faixa de Operação 3")
                    LaneLegendItem("LFO-4", "Linha de Faixa de Operação 4")
                    LaneLegendItem("LCA", "Linha de Continuidade / Canalização")
                    LaneLegendItem("LCO", "Linha de Canalização / Outros")
                    LaneLegendItem("ZPA", "Zona Proibida de Adiantamento / Área Zebrada")
                    LaneLegendItem("FTP", "Faixa de Travessia de Pedestres")
                    LaneLegendItem("LRE", "Linha de Retenção")
                    LaneLegendItem("LRV", "Linha de Redução de Velocidade")
                    LaneLegendItem("LDP", "Linha de Dê a Preferência")
                    LaneLegendItem("LEGENDA", "Inscrições no Pavimento (PARE, DEVAGAR, ESCOLA)")
                    LaneLegendItem("SETA PEM", "Seta de Mudança de Faixa / Posicionamento")
                    LaneLegendItem("SETA MOF", "Seta de Movimento Obrigatório de Faixa")
                    LaneLegendItem("SIP", "Símbolo de Inscrição no Pavimento (Ex: Cadeirante, Idoso)")
                    LaneLegendItem("Solo", "Placa de Solo / Suporte Térreo (Implantação / Remoção de Placa)")
                    LaneLegendItem("Aérea", "Placa Aérea / Pórtico ou Semipórtico (Implantação / Remoção de Placa)")
                }
            },
            confirmButton = {
                Button(onClick = { showLaneInfoDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun LaneLegendItem(code: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = code,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class GpsBadgeVisual(
    val containerColor: Color,
    val contentColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

@Composable
private fun GpsAccuracyBadge(
    accuracy: Float?,
    isFetching: Boolean,
    modifier: Modifier = Modifier
) {
    if (isFetching) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(11.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Buscando satélites...",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        val visual = when {
            accuracy == null -> GpsBadgeVisual(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                icon = Icons.Default.GpsFixed,
                label = "GPS Pronto"
            )
            accuracy <= 10f -> GpsBadgeVisual(
                containerColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32),
                icon = Icons.Default.CheckCircle,
                label = String.format(Locale.getDefault(), "Alta Precisão: ±%.0fm", accuracy)
            )
            accuracy <= 25f -> GpsBadgeVisual(
                containerColor = Color(0xFFFFF8E1),
                contentColor = Color(0xFFF57F17),
                icon = Icons.Default.Info,
                label = String.format(Locale.getDefault(), "Precisão Média: ±%.0fm", accuracy)
            )
            else -> GpsBadgeVisual(
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFD32F2F),
                icon = Icons.Default.Warning,
                label = String.format(Locale.getDefault(), "Baixa Precisão: ±%.0fm", accuracy)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .background(
                    color = visual.containerColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                tint = visual.contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = visual.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = visual.contentColor
            )
        }
    }
}

@Composable
private fun GpsLevelMeterItem(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isActive) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isActive) 1.5.dp else 0.5.dp,
                color = if (isActive) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


