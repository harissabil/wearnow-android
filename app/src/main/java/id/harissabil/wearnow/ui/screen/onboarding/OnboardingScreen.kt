package id.harissabil.wearnow.ui.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.harissabil.wearnow.ui.screen.onboarding.components.OnboardingStepIndicator
import id.harissabil.wearnow.ui.screen.onboarding.components.PhotoUploadStep
import id.harissabil.wearnow.ui.screen.onboarding.components.UploadProgressStep
import id.harissabil.wearnow.ui.screen.onboarding.components.WelcomeStep
import id.harissabil.wearnow.ui.screen.onboarding.utils.toFile
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(),
    onGoToHome: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUploadComplete) {
        if (uiState.isUploadComplete) {
            kotlinx.coroutines.delay(2000) // Show success state for 2 seconds
            onGoToHome()
        }
    }

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
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 24.dp)
        ) {
            // Top bar with back button and step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 0) {
                    IconButton(
                        onClick = { viewModel.previousStep() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(0.1f))
                }

                OnboardingStepIndicator(
                    currentStep = uiState.currentStep,
                    totalSteps = 3,
                    modifier = Modifier.weight(0.8f)
                )

                Spacer(modifier = Modifier.weight(0.1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main content area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState.currentStep) {
                        0 -> WelcomeStep()
                        1 -> PhotoUploadStep(
                            selectedImageUri = uiState.selectedImageUri,
                            onImageSelected = { uri ->
                                viewModel.setSelectedImage(uri)
                            }
                        )

                        2 -> UploadProgressStep(
                            isLoading = uiState.isLoading,
                            uploadProgress = uiState.uploadProgress,
                            isUploadComplete = uiState.isUploadComplete,
                            errorMessage = uiState.errorMessage
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.currentStep) {
                    0 -> {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    1 -> {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                uiState.selectedImageUri?.let { uri ->
                                    scope.launch {
                                        val file = uri.toFile(context)
                                        if (file != null) {
                                            viewModel.nextStep()
                                            viewModel.uploadUserPhoto(
                                                context,
                                                file
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                "Failed to process image. Please try again.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.selectedImageUri != null && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Upload",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    2 -> {
                        if (uiState.errorMessage != null) {
                            OutlinedButton(
                                onClick = { viewModel.previousStep() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Try Again",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Button(
                                onClick = { onGoToHome() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Skip",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        } else if (uiState.isUploadComplete) {
                            Button(
                                onClick = { onGoToHome() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Continue to App",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        // For loading state, no buttons are shown
                    }
                }
            }
        }
    }
}