package id.harissabil.wearnow.ui.screen.onboarding

import android.net.Uri

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null,
    val selectedImageUri: Uri? = null,
    val isUploadComplete: Boolean = false
)