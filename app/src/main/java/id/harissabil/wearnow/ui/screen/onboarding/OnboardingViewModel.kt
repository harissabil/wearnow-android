package id.harissabil.wearnow.ui.screen.onboarding

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.UserPhoto
import com.amplifyframework.storage.StoragePath
import com.amplifyframework.storage.options.StorageUploadFileOptions
import id.harissabil.wearnow.ui.screen.home.utils.ImageCompressionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        if (_uiState.value.currentStep < 2) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
        }
    }

    fun setSelectedImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            errorMessage = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun uploadUserPhoto(context: Context, imageFile: File) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    uploadProgress = 0f,
                    errorMessage = null
                )

                val identityId = getIdentityId()

                // Create unique filename
                val timestamp = System.currentTimeMillis()
                val key = "user-photos/$identityId/profile-$timestamp.jpg"

                // Compress the image before uploading
                Log.d(TAG, "Starting image compression...")
                _uiState.value = _uiState.value.copy(uploadProgress = 0.1f)

                val compressedFile = File(imageFile.parent, "compressed_${imageFile.name}")

                try {
                    ImageCompressionUtils.compressImageFile(
                        context = context,
                        sourceFile = imageFile,
                        targetFile = compressedFile
                    )

                    Log.d(TAG, "Image compression completed")
                    Log.d(
                        TAG,
                        "Original size: ${ImageCompressionUtils.getFileSizeString(imageFile)}"
                    )
                    Log.d(
                        TAG,
                        "Compressed size: ${ImageCompressionUtils.getFileSizeString(compressedFile)}"
                    )

                } catch (compressionError: Exception) {
                    Log.w(TAG, "Image compression failed, using original file", compressionError)
                    // If compression fails, use the original file
                    imageFile.copyTo(compressedFile, overwrite = true)
                }

                _uiState.value = _uiState.value.copy(uploadProgress = 0.2f)

                // Upload to S3 using compressed file
                val uploadedKey = uploadToS3(key, compressedFile) { progress ->
                    // Map upload progress to 0.2 - 0.9 range
                    val mappedProgress = 0.2f + (progress * 0.7f)
                    _uiState.value = _uiState.value.copy(uploadProgress = mappedProgress)
                }

                // Clean up compressed file
                try {
                    compressedFile.delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete compressed file", e)
                }

                _uiState.value = _uiState.value.copy(uploadProgress = 0.95f)

                // Create database record
                val currentUser = getCurrentUser()
                createUserPhotoRecord(currentUser.userId, uploadedKey)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    uploadProgress = 1f,
                    isUploadComplete = true
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload user photo", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    uploadProgress = 0f,
                    errorMessage = "Failed to upload photo: ${e.message}"
                )
            }
        }
    }

    fun uploadUserPhotoFromUri(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    uploadProgress = 0f,
                    errorMessage = null
                )

                val identityId = getIdentityId()

                // Create unique filename
                val timestamp = System.currentTimeMillis()
                val key = "user-photos/$identityId/profile-$timestamp.jpg"

                // Create temporary file for compressed image
                val tempDir = File(context.cacheDir, "temp_images")
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                }
                val compressedFile = File(tempDir, "compressed_profile_$timestamp.jpg")

                // Compress the image from URI
                Log.d(TAG, "Starting image compression from URI...")
                _uiState.value = _uiState.value.copy(uploadProgress = 0.1f)

                try {
                    ImageCompressionUtils.compressImageFromUri(
                        context = context,
                        uri = imageUri,
                        targetFile = compressedFile
                    )

                    Log.d(TAG, "Image compression completed")
                    Log.d(
                        TAG,
                        "Compressed size: ${ImageCompressionUtils.getFileSizeString(compressedFile)}"
                    )

                } catch (compressionError: Exception) {
                    Log.e(TAG, "Image compression from URI failed", compressionError)
                    throw compressionError
                }

                _uiState.value = _uiState.value.copy(uploadProgress = 0.2f)

                // Upload to S3 using compressed file
                val uploadedKey = uploadToS3(key, compressedFile) { progress ->
                    // Map upload progress to 0.2 - 0.9 range
                    val mappedProgress = 0.2f + (progress * 0.7f)
                    _uiState.value = _uiState.value.copy(uploadProgress = mappedProgress)
                }

                // Clean up compressed file
                try {
                    compressedFile.delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete compressed file", e)
                }

                _uiState.value = _uiState.value.copy(uploadProgress = 0.95f)

                // Create database record
                val currentUser = getCurrentUser()
                createUserPhotoRecord(currentUser.userId, uploadedKey)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    uploadProgress = 1f,
                    isUploadComplete = true
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload user photo from URI", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    uploadProgress = 0f,
                    errorMessage = "Failed to upload photo: ${e.message}"
                )
            }
        }
    }

    private suspend fun getIdentityId(): String = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoAuthSession = result as AWSCognitoAuthSession
                val identityId = cognitoAuthSession.identityIdResult.value
                continuation.resume(identityId as String)
            },
            { error -> continuation.resumeWithException(error) }
        )
    }

    private suspend fun getCurrentUser() = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.getCurrentUser(
            { user -> continuation.resume(user) },
            { error -> continuation.resumeWithException(error) }
        )
    }

    private suspend fun uploadToS3(
        key: String,
        file: File,
        onProgress: (Float) -> Unit,
    ): String = suspendCancellableCoroutine { continuation ->
        val options = StorageUploadFileOptions.builder()
            .build()

        Amplify.Storage.uploadFile(
            StoragePath.fromIdentityId { identityId ->
                key // Path will be: user-photos/{identityId}/profile-xxx.jpg
            },
            file,
            options,
            { progress ->
                val progressPercentage = progress.fractionCompleted.toFloat()
                onProgress(progressPercentage)
            },
            { result ->
                Log.d(TAG, "Upload completed successfully")
                continuation.resume(key) // Use the original key instead of result.key
            },
            { error ->
                Log.e(TAG, "Upload failed", error)
                continuation.resumeWithException(error)
            }
        )
    }

    private suspend fun createUserPhotoRecord(
        userId: String,
        photoUrl: String,
    ) = suspendCancellableCoroutine { continuation ->
        // Format date for Temporal.DateTime
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val currentDateString = dateFormat.format(Date())

        val userPhoto = UserPhoto.builder()
            .userId(userId)
            .photoUrl(photoUrl)
            .uploadedAt(Temporal.DateTime(currentDateString))
            .build()

        Amplify.API.mutate(
            ModelMutation.create(userPhoto),
            { response ->
                Log.d(TAG, "UserPhoto created: ${response.data}")
                continuation.resume(response.data)
            },
            { error ->
                Log.e(TAG, "Failed to create UserPhoto record", error)
                continuation.resumeWithException(error)
            }
        )
    }

    companion object {
        private const val TAG = "OnboardingViewModel"
    }
}