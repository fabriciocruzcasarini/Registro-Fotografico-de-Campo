package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.FieldActivityRepository
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.example.ui.screens.FormScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.FieldReportTheme
import com.example.ui.viewmodel.FieldActivityViewModel
import com.example.ui.viewmodel.UiEvent

enum class ScreenTab {
    FORM, HISTORY, SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FieldActivityRepository(database.fieldActivityDao())
        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            FieldReportTheme {
                val viewModel: FieldActivityViewModel = viewModel(
                    factory = FieldActivityViewModel.Factory(repository, preferencesManager)
                )

                var isSplashLoading by remember { mutableStateOf(true) }

                Crossfade(
                    targetState = isSplashLoading,
                    animationSpec = tween(400),
                    label = "SplashCrossfade"
                ) { showSplash ->
                    if (showSplash) {
                        SplashScreen(
                            onTimeout = { isSplashLoading = false }
                        )
                    } else {
                        MainAppScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: FieldActivityViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(ScreenTab.FORM) }

    val formState by viewModel.formState.collectAsState()
    val savedActivities by viewModel.savedActivities.collectAsState()
    val pendingCount = remember(savedActivities) { savedActivities.count { !it.isSent } }

    // Request permissions on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (locationGranted) {
            viewModel.fetchCurrentLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.fetchCurrentLocation(context)
        }
    }

    // Handle Toast Events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ActivitySaved -> {
                    val msg = if (event.sharedToWhatsApp) {
                        "Relatório salvo e enviado para o WhatsApp!"
                    } else {
                        "Relatório salvo com sucesso em Salvos/Histórico!"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            ScreenTab.FORM -> "Registro em Campo"
                            ScreenTab.HISTORY -> "Relatórios Salvos"
                            ScreenTab.SETTINGS -> "Configurações"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.FORM,
                    onClick = {
                        if (formState.isEditing) {
                            viewModel.resetFormForNewRecord()
                        }
                        currentTab = ScreenTab.FORM
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Novo Registro"
                        )
                    },
                    label = { Text("Novo", fontSize = 11.sp, maxLines = 1) }
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.HISTORY,
                    onClick = { currentTab = ScreenTab.HISTORY },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge {
                                        Text(pendingCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Salvos"
                            )
                        }
                    },
                    label = { Text("Salvos (${savedActivities.size})", fontSize = 11.sp, maxLines = 1) }
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { currentTab = ScreenTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações"
                        )
                    },
                    label = { Text("Ajustes", fontSize = 11.sp, maxLines = 1) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.FORM -> FormScreen(viewModel = viewModel)
                ScreenTab.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onNewRecordClick = {
                        if (formState.isEditing) {
                            viewModel.resetFormForNewRecord()
                        }
                        currentTab = ScreenTab.FORM
                    },
                    onEditActivity = { activity ->
                        viewModel.startEditing(activity)
                        currentTab = ScreenTab.FORM
                    }
                )
                ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
