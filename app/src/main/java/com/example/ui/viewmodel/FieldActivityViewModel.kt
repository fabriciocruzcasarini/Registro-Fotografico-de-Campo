package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.model.FieldActivity
import com.example.data.repository.FieldActivityRepository
import com.example.util.CsvReportExporter
import com.example.util.LocationHelper
import com.example.util.PdfReportExporter
import com.example.util.ShareUtil
import com.example.util.WatermarkUtil
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FormUiState(
    val operatorName: String = "",
    val highway: String = "",
    val direction: String = "Norte", // Norte, Sul, Leste, Oeste
    val laneType: String = "Principal", // Principal, Marginal, Dispositivo
    val activityType: String = "Selecione a Atividade",
    val studType: String = "Selecione o Tipo de Tacha",
    val plateType: String = "Selecione o Tipo de Placa",
    val plateCode: String = "",
    val plateText: String = "",
    val lane: String = "Selecione",
    val legendDescription: String = "",
    val eixo: String = "",
    val cadence: String = "",
    val observations: String = "",
    val kmStart: String = "",
    val kmEnd: String = "",
    val latitude: Double = -23.550520,
    val longitude: Double = -46.633308,
    val gpsAccuracy: Float? = null,
    val photoBeforeUri: Uri? = null,
    val photoDuringUri: Uri? = null,
    val photoAfterUri: Uri? = null,
    val isFetchingLocation: Boolean = false,
    val locationStatusText: String = "GPS Pronto",
    val isLocationFallback: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEditing: Boolean = false,
    val editingActivityId: Long? = null
)

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class ActivitySaved(val activity: FieldActivity, val sharedToWhatsApp: Boolean) : UiEvent
}

class FieldActivityViewModel(
    private val repository: FieldActivityRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    companion object {
        const val DEFAULT_ACTIVITY_PLACEHOLDER = "Selecione a Atividade"
        const val DEFAULT_STUD_PLACEHOLDER = "Selecione o Tipo de Tacha"
        const val DEFAULT_PLATE_TYPE_PLACEHOLDER = "Selecione o Tipo de Placa"
        const val DEFAULT_LANE_PLACEHOLDER = "Selecione"
    }

    val defaultOperatorName: String
        get() = preferencesManager?.defaultOperatorName ?: ""

    private val _photoCount = MutableStateFlow(preferencesManager?.photoCount ?: 2)
    val photoCount: StateFlow<Int> = _photoCount.asStateFlow()

    // Selection & Export States
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedActivityIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedActivityIds: StateFlow<Set<Long>> = _selectedActivityIds.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    fun setPhotoCount(count: Int) {
        preferencesManager?.photoCount = count
        _photoCount.value = count
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast("Configuração salva: $count fotos por registro."))
        }
    }

    private var sessionOperatorName: String = preferencesManager?.defaultOperatorName ?: ""

    fun saveDefaultOperatorName(name: String) {
        preferencesManager?.defaultOperatorName = name
        sessionOperatorName = name
        try {
            _formState.update { current ->
                if (!current.isEditing && current.operatorName.isBlank()) {
                    current.copy(operatorName = name)
                } else current
            }
        } catch (_: Exception) {}
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast("Nome padrão do operador salvo com sucesso!"))
        }
    }

    private fun createDefaultFormState(preserveOperator: Boolean = true): FormUiState {
        val configuredDefault = preferencesManager?.defaultOperatorName ?: ""
        val currentOperator = if (preserveOperator && sessionOperatorName.isNotBlank()) {
            sessionOperatorName
        } else {
            configuredDefault
        }
        return FormUiState(operatorName = currentOperator)
    }

    val directionOptions = listOf("Norte", "Sul", "Leste", "Oeste")
    val laneTypeOptions = listOf("Expressa", "Marginal", "Dispositivo")
    val activityOptions = listOf(
        DEFAULT_ACTIVITY_PLACEHOLDER,
        "Implantação Tacha",
        "Remoção Tacha",
        "Pintura Mecânica",
        "Pintura Manual",
        "Implantação Defensa",
        "Remoção Defensa",
        "Implantação de Placa",
        "Remoção de Placa"
    )

    val studTypeOptions = listOf(
        DEFAULT_STUD_PLACEHOLDER,
        "Tacha Mono Branca",
        "Tacha Mono Amarela",
        "Tacha Mono Vermelha",
        "Tacha Bi Branca",
        "Tacha Bi Branca / Vermelha",
        "Tachão"
    )

    val plateTypeOptions = listOf(
        DEFAULT_PLATE_TYPE_PLACEHOLDER,
        "Regulamentação",
        "Advertência",
        "Indicação"
    )

    fun isPlateActivity(activity: String): Boolean {
        return activity.contains("Placa", ignoreCase = true)
    }

    fun getLaneOptions(activityType: String): List<String> {
        return when {
            activityType == "Implantação Tacha" || activityType == "Remoção Tacha" -> listOf(
                DEFAULT_LANE_PLACEHOLDER,
                "LBO-D",
                "LBO-E",
                "LMS-1",
                "LMS-2",
                "LFO-1",
                "LFO-2",
                "LFO-3",
                "LFO-4",
                "LCA",
                "LCO",
                "ZPA"
            )
            activityType == "Pintura Mecânica" -> listOf(
                DEFAULT_LANE_PLACEHOLDER,
                "LBO-D",
                "LBO-E",
                "LMS-1",
                "LMS-2",
                "LCO",
                "LCA",
                "LFO-1",
                "LFO-2",
                "LFO-3",
                "LFO-4"
            )
            activityType == "Pintura Manual" -> listOf(
                DEFAULT_LANE_PLACEHOLDER,
                "FTP",
                "LRE",
                "LRV",
                "LEGENDA",
                "LDP",
                "ZPA",
                "SETA PEM",
                "SETA MOF",
                "SIP"
            )
            activityType == "Implantação Defensa" || activityType == "Remoção Defensa" -> listOf(
                DEFAULT_LANE_PLACEHOLDER,
                "LBO-D",
                "LBO-E"
            )
            isPlateActivity(activityType) -> listOf(
                DEFAULT_LANE_PLACEHOLDER,
                "Solo",
                "Aérea"
            )
            else -> listOf(DEFAULT_LANE_PLACEHOLDER)
        }
    }

    private val _formState = MutableStateFlow(createDefaultFormState())
    val formState: StateFlow<FormUiState> = _formState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val savedActivities: StateFlow<List<FieldActivity>> = repository.allActivities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateOperatorName(value: String) {
        sessionOperatorName = value
        _formState.update { it.copy(operatorName = value, errorMessage = null) }
    }

    fun updateHighway(value: String) {
        _formState.update { it.copy(highway = value, errorMessage = null) }
    }

    fun updateDirection(value: String) {
        _formState.update { it.copy(direction = value) }
    }

    fun updateLaneType(value: String) {
        _formState.update { it.copy(laneType = value) }
    }

    fun updateActivityType(value: String) {
        _formState.update { current ->
            val availableLanes = getLaneOptions(value)
            val updatedLane = if (current.lane in availableLanes) current.lane else DEFAULT_LANE_PLACEHOLDER
            val updatedStud = if (value == "Implantação Tacha") current.studType else DEFAULT_STUD_PLACEHOLDER
            val updatedLegendDesc = if (updatedLane == "LEGENDA") current.legendDescription else ""
            val isPlate = isPlateActivity(value)
            val updatedPlateType = if (isPlate) current.plateType else DEFAULT_PLATE_TYPE_PLACEHOLDER
            val updatedPlateCode = if (isPlate) current.plateCode else ""
            val updatedPlateText = if (isPlate && updatedPlateType == "Indicação") current.plateText else ""
            current.copy(
                activityType = value,
                studType = updatedStud,
                plateType = updatedPlateType,
                plateCode = updatedPlateCode,
                plateText = updatedPlateText,
                lane = updatedLane,
                legendDescription = updatedLegendDesc,
                errorMessage = null
            )
        }
    }

    fun updateStudType(value: String) {
        _formState.update { it.copy(studType = value, errorMessage = null) }
    }

    fun updatePlateType(value: String) {
        _formState.update { current ->
            current.copy(
                plateType = value,
                plateText = if (value == "Indicação") current.plateText else "",
                errorMessage = null
            )
        }
    }

    fun updatePlateCode(value: String) {
        _formState.update { it.copy(plateCode = value, errorMessage = null) }
    }

    fun updatePlateText(value: String) {
        _formState.update { it.copy(plateText = value, errorMessage = null) }
    }

    fun updateLane(value: String) {
        _formState.update { current ->
            current.copy(
                lane = value,
                legendDescription = if (value == "LEGENDA") current.legendDescription else "",
                errorMessage = null
            )
        }
    }

    fun updateLegendDescription(value: String) {
        _formState.update { it.copy(legendDescription = value, errorMessage = null) }
    }

    fun updateEixo(value: String) {
        _formState.update { it.copy(eixo = value) }
    }

    fun updateCadence(value: String) {
        _formState.update { it.copy(cadence = value) }
    }

    fun updateObservations(value: String) {
        _formState.update { it.copy(observations = value) }
    }

    fun updateKmStart(value: String) {
        _formState.update { it.copy(kmStart = value, errorMessage = null) }
    }

    fun updateKmEnd(value: String) {
        _formState.update { it.copy(kmEnd = value, errorMessage = null) }
    }

    fun setPhotoBefore(uri: Uri?) {
        _formState.update { it.copy(photoBeforeUri = uri, errorMessage = null) }
    }

    fun setPhotoDuring(uri: Uri?) {
        _formState.update { it.copy(photoDuringUri = uri, errorMessage = null) }
    }

    fun setPhotoAfter(uri: Uri?) {
        _formState.update { it.copy(photoAfterUri = uri, errorMessage = null) }
    }

    fun removePhotoBefore() {
        _formState.update { it.copy(photoBeforeUri = null) }
    }

    fun removePhotoDuring() {
        _formState.update { it.copy(photoDuringUri = null) }
    }

    fun removePhotoAfter() {
        _formState.update { it.copy(photoAfterUri = null) }
    }

    fun fetchCurrentLocation(context: Context) {
        _formState.update { it.copy(isFetchingLocation = true, locationStatusText = "Obtendo GPS...") }
        LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { result ->
                _formState.update {
                    it.copy(
                        latitude = result.latitude,
                        longitude = result.longitude,
                        gpsAccuracy = result.accuracy,
                        isLocationFallback = result.isFallback,
                        isFetchingLocation = false,
                        locationStatusText = if (result.isFallback) "GPS Padrão/Estimado" else "Coordenadas GPS Atualizadas"
                    )
                }
            },
            onError = { err ->
                _formState.update {
                    it.copy(
                        isLocationFallback = true,
                        isFetchingLocation = false,
                        locationStatusText = "GPS Padrão Utilizado"
                    )
                }
            }
        )
    }

    fun cleanupAllOrphanTempFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                val tempFiles = cacheDir.listFiles { file ->
                    file.name.startsWith("temp_photo_") || file.name.startsWith("sample_")
                }
                tempFiles?.forEach { file ->
                    if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fillSampleData(context: Context) {
        // Create sample photos using WatermarkUtil fallback generator if needed
        viewModelScope.launch(Dispatchers.IO) {
            val isThreePhotos = _photoCount.value == 3
            val bitmapBefore = WatermarkUtil.createFallbackBitmap("FOTO ANTES - SERVIÇO EM CAMPO")
            val bitmapDuring = if (isThreePhotos) WatermarkUtil.createFallbackBitmap("FOTO DURANTE - EXECUÇÃO EM CAMPO") else null
            val bitmapAfter = WatermarkUtil.createFallbackBitmap("FOTO DEPOIS - SERVIÇO CONCLUÍDO")

            val fileBefore = java.io.File(context.cacheDir, "sample_before.jpg")
            val fileDuring = if (isThreePhotos) java.io.File(context.cacheDir, "sample_during.jpg") else null
            val fileAfter = java.io.File(context.cacheDir, "sample_after.jpg")

            java.io.FileOutputStream(fileBefore).use { bitmapBefore.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            fileDuring?.let { f ->
                java.io.FileOutputStream(f).use { bitmapDuring?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            }
            java.io.FileOutputStream(fileAfter).use { bitmapAfter.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }

            val uriBefore = Uri.fromFile(fileBefore)
            val uriDuring = fileDuring?.let { Uri.fromFile(it) }
            val uriAfter = Uri.fromFile(fileAfter)

            _formState.update {
                it.copy(
                    operatorName = if (it.operatorName.isNotBlank()) it.operatorName else "João Silva",
                    highway = "BR-101",
                    direction = "Norte",
                    laneType = "Principal",
                    activityType = "Implantação Tacha",
                    studType = "Tacha Mono Branca",
                    plateType = DEFAULT_PLATE_TYPE_PLACEHOLDER,
                    plateCode = "",
                    plateText = "",
                    lane = "LBO-D",
                    legendDescription = "",
                    eixo = "Eixo Principal",
                    cadence = "1:3 (3 metros)",
                    observations = "Aplicação de tachas monodirecionais com resina epoxy.",
                    kmStart = "120.5",
                    kmEnd = "121.2",
                    latitude = -23.550520,
                    longitude = -46.633308,
                    photoBeforeUri = uriBefore,
                    photoDuringUri = uriDuring,
                    photoAfterUri = uriAfter,
                    errorMessage = null
                )
            }

            _uiEvent.emit(UiEvent.ShowToast("Dados de exemplo preenchidos com fotos!"))
        }
    }

    fun startEditing(activity: FieldActivity) {
        val uriBefore = if (activity.photoBeforePath.isNotBlank()) {
            val file = java.io.File(activity.photoBeforePath)
            if (file.exists()) Uri.fromFile(file) else null
        } else null

        val uriDuring = if (activity.photoDuringPath.isNotBlank()) {
            val file = java.io.File(activity.photoDuringPath)
            if (file.exists()) Uri.fromFile(file) else null
        } else null

        val uriAfter = if (activity.photoAfterPath.isNotBlank()) {
            val file = java.io.File(activity.photoAfterPath)
            if (file.exists()) Uri.fromFile(file) else null
        } else null

        val stud = if (activity.studType.isNotBlank()) activity.studType else DEFAULT_STUD_PLACEHOLDER
        val plateType = if (activity.plateType.isNotBlank()) activity.plateType else DEFAULT_PLATE_TYPE_PLACEHOLDER
        val lane = if (activity.lane.isNotBlank()) activity.lane else DEFAULT_LANE_PLACEHOLDER

        _formState.update {
            it.copy(
                operatorName = activity.operatorName,
                highway = activity.highway,
                direction = activity.direction,
                laneType = activity.laneType,
                activityType = activity.activityType,
                studType = stud,
                plateType = plateType,
                plateCode = activity.plateCode,
                plateText = activity.plateText,
                lane = lane,
                legendDescription = activity.legendDescription,
                eixo = activity.eixo,
                cadence = activity.cadence,
                observations = activity.observations,
                kmStart = activity.kmStart,
                kmEnd = activity.kmEnd,
                latitude = activity.latitude,
                longitude = activity.longitude,
                photoBeforeUri = uriBefore,
                photoDuringUri = uriDuring,
                photoAfterUri = uriAfter,
                isEditing = true,
                editingActivityId = activity.id,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast("Modo de edição ativado para o Registro #${activity.id}"))
        }
    }

    fun cancelEditing() {
        _formState.update { createDefaultFormState() }
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast("Edição cancelada. Formulário limpo."))
        }
    }

    fun resetFormForNewRecord() {
        _formState.update { createDefaultFormState() }
    }

    fun validateForm(): String? {
        val state = _formState.value
        if (state.operatorName.isBlank()) return "Informe o Nome do Encarregado."
        if (state.activityType.isBlank() || state.activityType == DEFAULT_ACTIVITY_PLACEHOLDER) {
            return "Selecione uma Atividade válida para continuar."
        }
        if (state.activityType == "Implantação Tacha" && (state.studType.isBlank() || state.studType == DEFAULT_STUD_PLACEHOLDER)) {
            return "Selecione o Tipo de Tacha para a atividade de Implantação."
        }
        if (isPlateActivity(state.activityType) && (state.plateType.isBlank() || state.plateType == DEFAULT_PLATE_TYPE_PLACEHOLDER)) {
            return "Selecione o Tipo de Placa."
        }
        if (isPlateActivity(state.activityType) && state.plateType == "Indicação" && state.plateText.isBlank()) {
            return "Informe o Texto da Placa de Indicação."
        }
        if (state.lane.equals("LEGENDA", ignoreCase = true) && state.legendDescription.isBlank()) {
            return "Informe a Descrição da Legenda."
        }
        if (state.highway.isBlank()) return "Informe a Rodovia."
        if (state.kmStart.isBlank()) return "Informe o KM Inicial."
        if (state.kmEnd.isBlank() && !isPlateActivity(state.activityType)) {
            return "Informe o KM Final."
        }
        if (state.photoBeforeUri == null) return "Por favor, tire a foto do ANTES do serviço."
        if (_photoCount.value == 3 && state.photoDuringUri == null) {
            return "Por favor, tire a foto do DURANTE a execução do serviço."
        }
        if (state.photoAfterUri == null) return "Por favor, tire a foto do DEPOIS do serviço."
        return null
    }

    fun saveActivity(context: Context, shareImmediatelyToWhatsApp: Boolean) {
        val validationError = validateForm()
        if (validationError != null) {
            _formState.update { it.copy(errorMessage = validationError) }
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast(validationError))
            }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = _formState.value
                val isEditingMode = state.isEditing && state.editingActivityId != null
                val timestamp = System.currentTimeMillis()

                val laneValue = if (state.lane == DEFAULT_LANE_PLACEHOLDER) "" else state.lane
                val studTypeValue = if (state.activityType == "Implantação Tacha" && state.studType != DEFAULT_STUD_PLACEHOLDER) {
                    state.studType
                } else {
                    ""
                }
                val isPlate = isPlateActivity(state.activityType)
                val plateTypeValue = if (isPlate && state.plateType != DEFAULT_PLATE_TYPE_PLACEHOLDER) {
                    state.plateType
                } else {
                    ""
                }
                val plateCodeValue = if (isPlate) state.plateCode.trim() else ""
                val plateTextValue = if (isPlate && plateTypeValue == "Indicação") state.plateText.trim() else ""

                val legendDescValue = if (laneValue.equals("LEGENDA", ignoreCase = true)) state.legendDescription else ""

                val kmStartVal = state.kmStart.trim()
                val kmEndVal = if (state.kmEnd.isBlank()) kmStartVal else state.kmEnd.trim()

                // Generate watermarked photos
                val photoBeforePath = WatermarkUtil.createWatermarkedPhoto(
                    context = context,
                    sourceUri = state.photoBeforeUri!!,
                    photoType = WatermarkUtil.PhotoType.BEFORE,
                    operatorName = state.operatorName,
                    highway = state.highway,
                    direction = state.direction,
                    laneType = state.laneType,
                    activityType = state.activityType,
                    studType = studTypeValue,
                    plateType = plateTypeValue,
                    plateCode = plateCodeValue,
                    plateText = plateTextValue,
                    lane = laneValue,
                    legendDescription = legendDescValue,
                    eixo = state.eixo,
                    cadence = state.cadence,
                    observations = state.observations,
                    kmStart = kmStartVal,
                    kmEnd = kmEndVal,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    timestampMs = timestamp
                )

                val photoDuringPath = if (_photoCount.value == 3 && state.photoDuringUri != null) {
                    WatermarkUtil.createWatermarkedPhoto(
                        context = context,
                        sourceUri = state.photoDuringUri,
                        photoType = WatermarkUtil.PhotoType.DURING,
                        operatorName = state.operatorName,
                        highway = state.highway,
                        direction = state.direction,
                        laneType = state.laneType,
                        activityType = state.activityType,
                        studType = studTypeValue,
                        plateType = plateTypeValue,
                        plateCode = plateCodeValue,
                        plateText = plateTextValue,
                        lane = laneValue,
                        legendDescription = legendDescValue,
                        eixo = state.eixo,
                        cadence = state.cadence,
                        observations = state.observations,
                        kmStart = kmStartVal,
                        kmEnd = kmEndVal,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        timestampMs = timestamp
                    )
                } else {
                    ""
                }

                val photoAfterPath = WatermarkUtil.createWatermarkedPhoto(
                    context = context,
                    sourceUri = state.photoAfterUri!!,
                    photoType = WatermarkUtil.PhotoType.AFTER,
                    operatorName = state.operatorName,
                    highway = state.highway,
                    direction = state.direction,
                    laneType = state.laneType,
                    activityType = state.activityType,
                    studType = studTypeValue,
                    plateType = plateTypeValue,
                    plateCode = plateCodeValue,
                    plateText = plateTextValue,
                    lane = laneValue,
                    legendDescription = legendDescValue,
                    eixo = state.eixo,
                    cadence = state.cadence,
                    observations = state.observations,
                    kmStart = kmStartVal,
                    kmEnd = kmEndVal,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    timestampMs = timestamp
                )

                val activity = FieldActivity(
                    id = if (isEditingMode) state.editingActivityId!! else 0,
                    operatorName = state.operatorName,
                    highway = state.highway,
                    direction = state.direction,
                    laneType = state.laneType,
                    activityType = state.activityType,
                    studType = studTypeValue,
                    plateType = plateTypeValue,
                    plateCode = plateCodeValue,
                    plateText = plateTextValue,
                    lane = laneValue,
                    legendDescription = legendDescValue,
                    eixo = state.eixo,
                    cadence = state.cadence,
                    observations = state.observations,
                    kmStart = kmStartVal,
                    kmEnd = kmEndVal,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    timestamp = timestamp,
                    photoBeforePath = photoBeforePath,
                    photoDuringPath = photoDuringPath,
                    photoAfterPath = photoAfterPath,
                    isSent = false
                )

                val finalId: Long
                if (isEditingMode) {
                    val oldActivity = savedActivities.value.firstOrNull { it.id == state.editingActivityId }
                    if (oldActivity != null) {
                        if (oldActivity.photoBeforePath.isNotBlank() && oldActivity.photoBeforePath != photoBeforePath) {
                            try { File(oldActivity.photoBeforePath).delete() } catch (_: Exception) {}
                        }
                        if (oldActivity.photoDuringPath.isNotBlank() && oldActivity.photoDuringPath != photoDuringPath) {
                            try { File(oldActivity.photoDuringPath).delete() } catch (_: Exception) {}
                        }
                        if (oldActivity.photoAfterPath.isNotBlank() && oldActivity.photoAfterPath != photoAfterPath) {
                            try { File(oldActivity.photoAfterPath).delete() } catch (_: Exception) {}
                        }
                    }
                    repository.update(activity)
                    finalId = state.editingActivityId!!
                } else {
                    finalId = repository.insert(activity)
                }

                val savedActivity = activity.copy(id = finalId)

                var shared = false
                if (shareImmediatelyToWhatsApp) {
                    withContext(Dispatchers.Main) {
                        shared = ShareUtil.shareActivityToWhatsApp(context, savedActivity)
                    }
                    if (shared) {
                        repository.updateSentStatus(finalId, true)
                    }
                }

                _formState.update { createDefaultFormState() } // Reset form

                _uiEvent.emit(
                    UiEvent.ActivitySaved(
                        activity = savedActivity.copy(isSent = shared),
                        sharedToWhatsApp = shared
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _formState.update { it.copy(isSaving = false, errorMessage = "Erro ao salvar atividade: ${e.localizedMessage}") }
                _uiEvent.emit(UiEvent.ShowToast("Erro ao salvar: ${e.localizedMessage}"))
            }
        }
    }

    fun shareExistingActivity(context: Context, activity: FieldActivity) {
        viewModelScope.launch {
            val shared = ShareUtil.shareActivityToWhatsApp(context, activity)
            if (shared && !activity.isSent) {
                repository.updateSentStatus(activity.id, true)
                _uiEvent.emit(UiEvent.ShowToast("Relatório compartilhado com sucesso!"))
            }
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedActivityIds.value = emptySet()
        }
    }

    fun toggleActivitySelection(id: Long) {
        _selectedActivityIds.update { current ->
            if (current.contains(id)) {
                current - id
            } else {
                current + id
            }
        }
    }

    fun selectAllActivities(activities: List<FieldActivity>) {
        _selectedActivityIds.value = activities.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedActivityIds.value = emptySet()
    }

    fun exportSelectedPdf(context: Context, activities: List<FieldActivity>) {
        val ids = _selectedActivityIds.value
        val toExport = if (ids.isEmpty()) activities else activities.filter { it.id in ids }
        if (toExport.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Nenhum registro para exportação em PDF."))
            }
            return
        }

        _isExporting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = PdfReportExporter.exportAndSharePdf(context, toExport)
                if (success) {
                    _uiEvent.emit(UiEvent.ShowToast("Relatório PDF com ${toExport.size} registros gerado com sucesso!"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erro ao exportar PDF: ${e.localizedMessage}"))
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportSelectedCsv(context: Context, activities: List<FieldActivity>) {
        val ids = _selectedActivityIds.value
        val toExport = if (ids.isEmpty()) activities else activities.filter { it.id in ids }
        if (toExport.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Nenhum registro para exportação em CSV."))
            }
            return
        }

        _isExporting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = CsvReportExporter.exportAndShareCsv(context, toExport)
                if (success) {
                    _uiEvent.emit(UiEvent.ShowToast("Planilha CSV com ${toExport.size} registros gerada com sucesso!"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erro ao exportar CSV: ${e.localizedMessage}"))
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun deleteActivity(activity: FieldActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (activity.photoBeforePath.isNotBlank()) {
                    val fileBefore = File(activity.photoBeforePath)
                    if (fileBefore.exists()) {
                        fileBefore.delete()
                    }
                }
                if (activity.photoDuringPath.isNotBlank()) {
                    val fileDuring = File(activity.photoDuringPath)
                    if (fileDuring.exists()) {
                        fileDuring.delete()
                    }
                }
                if (activity.photoAfterPath.isNotBlank()) {
                    val fileAfter = File(activity.photoAfterPath)
                    if (fileAfter.exists()) {
                        fileAfter.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.delete(activity)
            _uiEvent.emit(UiEvent.ShowToast("Registro e fotos excluídos com sucesso."))
        }
    }

    class Factory(
        private val repository: FieldActivityRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FieldActivityViewModel(repository, preferencesManager) as T
        }
    }
}
