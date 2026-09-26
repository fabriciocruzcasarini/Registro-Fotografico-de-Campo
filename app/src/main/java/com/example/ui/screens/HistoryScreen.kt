package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FieldActivity
import com.example.ui.components.WatermarkPreviewModal
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.FieldActivityViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryFilter {
    ALL, PENDING, SENT
}

@Composable
fun HistoryScreen(
    viewModel: FieldActivityViewModel,
    onEditActivity: (FieldActivity) -> Unit = {},
    onNewRecordClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedActivities by viewModel.savedActivities.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedActivityIds by viewModel.selectedActivityIds.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()

    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var selectedActivityForPreview by remember { mutableStateOf<FieldActivity?>(null) }
    var activityToDelete by remember { mutableStateOf<FieldActivity?>(null) }
    var showExportAllDialog by remember { mutableStateOf(false) }

    val filteredList = remember(savedActivities, filter) {
        when (filter) {
            HistoryFilter.ALL -> savedActivities
            HistoryFilter.PENDING -> savedActivities.filter { !it.isSent }
            HistoryFilter.SENT -> savedActivities.filter { it.isSent }
        }
    }

    val allFilteredSelected = remember(filteredList, selectedActivityIds) {
        filteredList.isNotEmpty() && filteredList.all { it.id in selectedActivityIds }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Selection Mode Contextual Bar or Regular Filter Toolbar
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { viewModel.setSelectionMode(false) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar Seleção",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${selectedActivityIds.size} de ${filteredList.size} selecionados",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        TextButton(
                            onClick = {
                                if (allFilteredSelected) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.selectAllActivities(filteredList)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (allFilteredSelected) "Desmarcar" else "Todos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Regular Filter Bar + Quick Export Action
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Chips (with horizontal scroll)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )

                        FilterChip(
                            selected = filter == HistoryFilter.ALL,
                            onClick = { filter = HistoryFilter.ALL },
                            label = { Text("Todos (${savedActivities.size})", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = filter == HistoryFilter.PENDING,
                            onClick = { filter = HistoryFilter.PENDING },
                            label = { Text("Pendentes (${savedActivities.count { !it.isSent }})", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = filter == HistoryFilter.SENT,
                            onClick = { filter = HistoryFilter.SENT },
                            label = { Text("Enviados (${savedActivities.count { it.isSent }})", fontSize = 11.sp) }
                        )
                    }

                    if (savedActivities.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                viewModel.setSelectionMode(true)
                                viewModel.selectAllActivities(filteredList)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Modo Seleção",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // List Content
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (filter) {
                                HistoryFilter.ALL -> "Nenhum registro salvo ainda."
                                HistoryFilter.PENDING -> "Nenhum relatório pendente de envio."
                                HistoryFilter.SENT -> "Nenhum relatório enviado ainda."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Preencha o formulário para salvar ou enviar relatórios de campo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onNewRecordClick,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Novo Registro",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Criar Novo Registro", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { activity ->
                        val isSelected = activity.id in selectedActivityIds

                        ActivityHistoryCard(
                            activity = activity,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelection = { viewModel.toggleActivitySelection(activity.id) },
                            onPreviewClick = { selectedActivityForPreview = activity },
                            onEditClick = { onEditActivity(activity) },
                            onShareClick = { viewModel.shareExistingActivity(context, activity) },
                            onExportSinglePdf = { viewModel.exportSelectedPdf(context, listOf(activity)) },
                            onDeleteClick = { activityToDelete = activity }
                        )
                    }
                    item {
                        // Extra bottom spacing to avoid FAB or Export Action Bar overlap
                        Spacer(modifier = Modifier.height(if (isSelectionMode) 96.dp else 72.dp))
                    }
                }
            }
        }

        // Floating Action Button for New Record (Only shown when not in selection mode)
        if (!isSelectionMode) {
            ExtendedFloatingActionButton(
                onClick = onNewRecordClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Novo Registro") },
                text = { Text("Novo Registro", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Floating Bottom Action Bar for Export (Shown when in Selection Mode)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedActivityIds.size} registro(s) selecionado(s)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isExporting) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gerando...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PDF Export Button
                        Button(
                            onClick = {
                                viewModel.exportSelectedPdf(context, savedActivities)
                            },
                            enabled = selectedActivityIds.isNotEmpty() && !isExporting,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Exportar PDF",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Relatório PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }

                        // CSV Export Button
                        FilledTonalButton(
                            onClick = {
                                viewModel.exportSelectedCsv(context, savedActivities)
                            },
                            enabled = selectedActivityIds.isNotEmpty() && !isExporting,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = "Exportar CSV",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Planilha CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    // Watermark Preview Modal
    selectedActivityForPreview?.let { activity ->
        WatermarkPreviewModal(
            activity = activity,
            onDismiss = { selectedActivityForPreview = null },
            onShareWhatsApp = { viewModel.shareExistingActivity(context, it) },
            onExportPdf = { viewModel.exportSelectedPdf(context, listOf(it)) }
        )
    }

    // Delete Confirmation Dialog
    activityToDelete?.let { activity ->
        AlertDialog(
            onDismissRequest = { activityToDelete = null },
            title = { Text("Excluir Registro") },
            text = { Text("Deseja realmente excluir o relatório de ${activity.highway} - KM ${activity.kmStart}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteActivity(activity)
                        activityToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { activityToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ActivityHistoryCard(
    activity: FieldActivity,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onExportSinglePdf: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activity.timestamp))

    val cardBorder = if (isSelectionMode && isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    val cardBgColor = if (isSelectionMode && isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardBorder)
            .clickable {
                if (isSelectionMode) {
                    onToggleSelection()
                } else {
                    onPreviewClick()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection() },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.highway,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sentido ${activity.direction} • Pista ${activity.laneType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (activity.isSent) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (activity.isSent) Icons.Default.CheckCircle else Icons.Default.Pending,
                        contentDescription = null,
                        tint = if (activity.isSent) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (activity.isSent) "Enviado WhatsApp" else "Pendente",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (activity.isSent) Color(0xFF15803D) else Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val activityText = buildString {
                    append("Atividade: ${activity.activityType}")
                    if (activity.studType.isNotBlank() && activity.studType != "Selecione o Tipo de Tacha") {
                        append(" (${activity.studType})")
                    }
                    if (activity.plateType.isNotBlank() && activity.plateType != "Selecione o Tipo de Placa") {
                        append(" (${activity.plateType}${if (activity.plateCode.isNotBlank()) " - ${activity.plateCode}" else ""})")
                    }
                }
                Text(
                    text = activityText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val detailsLine = buildString {
                    append("Encarregado: ${activity.operatorName} | KM ${activity.kmStart} ao ${activity.kmEnd}")
                    val laneDisplay = when {
                        activity.lane.isBlank() -> ""
                        activity.lane.equals("LEGENDA", ignoreCase = true) && activity.legendDescription.isNotBlank() -> "${activity.lane} (${activity.legendDescription})"
                        else -> activity.lane
                    }
                    if (laneDisplay.isNotBlank()) append(" | Faixa: $laneDisplay")
                    if (activity.plateText.isNotBlank()) append(" | Texto: \"${activity.plateText}\"")
                    if (activity.eixo.isNotBlank()) append(" | Eixo: ${activity.eixo}")
                    if (activity.cadence.isNotBlank()) append(" | Cadência: ${activity.cadence}")
                }

                Text(
                    text = detailsLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (activity.observations.isNotBlank()) {
                    Text(
                        text = "Obs: ${activity.observations}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "GPS: Lat %.6f, Long %.6f • %s", activity.latitude, activity.longitude, dateStr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Photo Thumbnails Row (proportional 1:1 aspect ratio)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPreviewClick() },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Antes Thumbnail
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = File(activity.photoBeforePath),
                        contentDescription = "Foto Antes",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ANTES",
                            color = Color(0xFFEAB308),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                // Durante Thumbnail (if present)
                if (activity.photoDuringPath.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = File(activity.photoDuringPath),
                            contentDescription = "Foto Durante",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color(0xCC0F172A))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DURANTE",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Depois Thumbnail
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = File(activity.photoAfterPath),
                        contentDescription = "Foto Depois",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DEPOIS",
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions (Hidden in Selection Mode to keep interface clean)
            if (!isSelectionMode) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: View Photos & Edit (50% - 50%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onPreviewClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Ver Fotos",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Fotos", fontSize = 12.sp, maxLines = 1)
                        }

                        FilledTonalButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }

                    // Row 2: WhatsApp Share, Single PDF Export & Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onShareClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Enviar WhatsApp",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onExportSinglePdf,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
