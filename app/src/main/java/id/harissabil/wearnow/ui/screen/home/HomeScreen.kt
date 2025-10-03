package id.harissabil.wearnow.ui.screen.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import id.harissabil.wearnow.ui.screen.home.components.CameraPreview
import id.harissabil.wearnow.ui.screen.home.components.ProcessingOverlay
import id.harissabil.wearnow.ui.screen.home.components.TryOnOptionsDialog
import id.harissabil.wearnow.ui.screen.home.components.UserPhotoSelector
import id.harissabil.wearnow.ui.screen.onboarding.utils.toFile
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToResult: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setCameraReady(true)
        }
    }

    // Request camera permission on first launch
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            viewModel.setCameraReady(true)
        }
    }

    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WearNOW",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Virtual Try-On Studio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        )
                    }

                    // Sign out button
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.signOut { success ->
                                if (success) {
                                    onNavigateToAuth()
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        // Main content with gradient background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // User photos section
            UserPhotoSelector(
                userPhotos = uiState.userPhotos,
                selectedUserPhoto = uiState.selectedUserPhoto,
                onUserPhotoSelected = { userPhoto ->
                    viewModel.setSelectedUserPhoto(userPhoto)
                },
                onAddPhoto = { uri ->
                    scope.launch {
                        viewModel.uploadUserPhoto(context, uri) { success ->
                            if (success) {
                                // Photo uploaded successfully, list will refresh automatically
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Photo uploaded successfully!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                            // Error handling is done in the ViewModel and will show in snackbar
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Camera preview section
            if (cameraPermissionState.status.isGranted && uiState.isCameraReady) {
                CameraPreview(
                    capturedGarmentUri = uiState.capturedGarmentUri,
                    selectedGarmentUri = uiState.selectedGarmentUri,
                    onImageCaptured = { uri ->
                        viewModel.setCapturedGarment(uri)
                    },
                    onGalleryImageSelected = { uri ->
                        viewModel.setSelectedGarment(uri)
                    },
                    onCameraReady = { ready ->
                        viewModel.setCameraReady(ready)
                    },
                    onClearCapturedImage = {
                        viewModel.clearCapturedGarment()
                    },
                    onClearSelectedImage = {
                        viewModel.clearSelectedGarment()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                CameraPermissionCard(
                    onRequestPermission = {
                        if (cameraPermissionState.status.shouldShowRationale) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Camera permission is required for capturing garment photos"
                                )
                            }
                        }
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Action buttons
            ActionButtons(
                hasGarmentImage = uiState.capturedGarmentUri != null || uiState.selectedGarmentUri != null,
                hasUserPhoto = uiState.selectedUserPhoto != null,
                isProcessing = uiState.isProcessing,
                onStartTryOn = {
                    val garmentUri = uiState.capturedGarmentUri ?: uiState.selectedGarmentUri
                    if (garmentUri != null) {
                        scope.launch {
                            val garmentFile = garmentUri.toFile(context)
                            if (garmentFile != null) {
                                viewModel.startVirtualTryOn(context, garmentFile) { historyId ->
                                    historyId?.let {
                                        onNavigateToResult(it)
                                        onNavigateToResult(it)
                                    }
                                }
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Failed to process garment image. Please try again.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Try-on options preview
            TryOnOptionsPreview(
                selectedGarmentClass = uiState.selectedGarmentClass,
                selectedMergeStyle = uiState.selectedMergeStyle,
                onOptionsClick = { viewModel.showOptionsDialog() },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Options dialog
        if (uiState.showOptionsDialog) {
            TryOnOptionsDialog(
                selectedGarmentClass = uiState.selectedGarmentClass,
                selectedMergeStyle = uiState.selectedMergeStyle,
                onGarmentClassSelected = { garmentClass ->
                    viewModel.setGarmentClass(garmentClass)
                },
                onMergeStyleSelected = { mergeStyle ->
                    viewModel.setMergeStyle(mergeStyle)
                },
                onConfirm = {
                    viewModel.hideOptionsDialog()
                    val garmentUri = uiState.capturedGarmentUri ?: uiState.selectedGarmentUri
                    if (garmentUri != null) {
                        scope.launch {
                            val garmentFile = garmentUri.toFile(context)
                            if (garmentFile != null) {
                                viewModel.startVirtualTryOn(context, garmentFile) { historyId ->
                                    historyId?.let {
                                        onNavigateToResult(it)
                                    }
                                }
                            }
                        }
                    }
                },
                onDismiss = { viewModel.hideOptionsDialog() }
            )
        }

        // Processing overlay
        ProcessingOverlay(
            isProcessing = uiState.isProcessing,
            processingMessage = uiState.processingProgress
        )
    }
}

@Composable
private fun CameraPermissionCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grant camera access to capture garment photos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun TryOnOptionsPreview(
    selectedGarmentClass: GarmentClass,
    selectedMergeStyle: MergeStyle,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Try-On Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedButton(
                    onClick = onOptionsClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Customize",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedGarmentClass.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Style",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedMergeStyle.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    hasGarmentImage: Boolean,
    hasUserPhoto: Boolean,
    isProcessing: Boolean,
    onStartTryOn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartTryOn,
            modifier = Modifier.fillMaxWidth(),
            enabled = hasGarmentImage && hasUserPhoto && !isProcessing,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = when {
                    isProcessing -> "Processing..."
                    !hasUserPhoto -> "Select User Photo First"
                    !hasGarmentImage -> "Capture Garment First"
                    else -> "Start Virtual Try-On"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}