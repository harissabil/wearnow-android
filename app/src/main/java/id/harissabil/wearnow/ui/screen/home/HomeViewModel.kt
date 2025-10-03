package id.harissabil.wearnow.ui.screen.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.TryOnHistory
import com.amplifyframework.datastore.generated.model.TryOnHistoryStatus
import com.amplifyframework.datastore.generated.model.UserPhoto
import com.amplifyframework.storage.StoragePath
import com.amplifyframework.storage.options.StorageUploadFileOptions
import id.harissabil.wearnow.ui.screen.home.utils.ImageCompressionUtils
import id.harissabil.wearnow.ui.screen.home.utils.VirtualTryOnService
import kotlinx.coroutines.Dispatchers
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

enum class GarmentClass(val displayName: String, val description: String) {
    UPPER_BODY("Upper Body", "Shirts, t-shirts, jackets, sweaters"),
    LOWER_BODY("Lower Body", "Pants, shorts, skirts"),
    FULL_BODY("Full Body", "Dresses, jumpsuits, full outfits"),
    FOOTWEAR("Footwear", "Shoes, boots, sandals")
}

enum class MergeStyle(val displayName: String, val description: String) {
    BALANCED("Balanced", "Good balance between realism and garment visibility"),
    SEAMLESS("Seamless", "More natural blending with user photo"),
    DETAILED("Detailed", "Preserves more garment details")
}

enum class TryOnStatus {
    PROCESSING, COMPLETED, FAILED
}

data class HomeUiState(
    val userPhotos: List<UserPhoto> = emptyList(),
    val selectedUserPhoto: UserPhoto? = null,
    val capturedGarmentUri: Uri? = null,
    val selectedGarmentUri: Uri? = null,
    val selectedGarmentClass: GarmentClass = GarmentClass.UPPER_BODY,
    val selectedMergeStyle: MergeStyle = MergeStyle.BALANCED,
    val isProcessing: Boolean = false,
    val processingProgress: String = "",
    val errorMessage: String? = null,
    val showOptionsDialog: Boolean = false,
    val isCameraReady: Boolean = false,
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserPhotos()
    }

    fun setCameraReady(ready: Boolean) {
        _uiState.value = _uiState.value.copy(isCameraReady = ready)
    }

    fun setSelectedUserPhoto(userPhoto: UserPhoto) {
        _uiState.value = _uiState.value.copy(selectedUserPhoto = userPhoto)
    }

    fun setCapturedGarment(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            capturedGarmentUri = uri,
            selectedGarmentUri = null,
            errorMessage = null
        )
    }

    fun setSelectedGarment(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedGarmentUri = uri,
            capturedGarmentUri = null,
            errorMessage = null
        )
    }

    fun setGarmentClass(garmentClass: GarmentClass) {
        _uiState.value = _uiState.value.copy(selectedGarmentClass = garmentClass)
    }

    fun setMergeStyle(mergeStyle: MergeStyle) {
        _uiState.value = _uiState.value.copy(selectedMergeStyle = mergeStyle)
    }

    fun showOptionsDialog() {
        _uiState.value = _uiState.value.copy(showOptionsDialog = true)
    }

    fun hideOptionsDialog() {
        _uiState.value = _uiState.value.copy(showOptionsDialog = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearCapturedGarment() {
        _uiState.value = _uiState.value.copy(
            capturedGarmentUri = null,
            errorMessage = null
        )
    }

    fun clearSelectedGarment() {
        _uiState.value = _uiState.value.copy(
            selectedGarmentUri = null,
            errorMessage = null
        )
    }

    fun startVirtualTryOn(context: Context, garmentFile: File, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val selectedUserPhoto = currentState.selectedUserPhoto

                if (selectedUserPhoto == null) {
                    _uiState.value = currentState.copy(errorMessage = "Please select a user photo")
                    onComplete(null)
                    return@launch
                }

                _uiState.value = currentState.copy(
                    isProcessing = true,
                    processingProgress = "Compressing and uploading garment photo...",
                    errorMessage = null
                )

                // Get identity ID for consistent storage paths
                val identityId = VirtualTryOnService.getIdentityId()

                // 1. Upload garment photo (with compression)
                _uiState.value =
                    _uiState.value.copy(processingProgress = "Compressing and uploading garment photo...")
                val garmentUploadResult = uploadGarmentPhoto(context, garmentFile, identityId)

                // 2. Create history record
                _uiState.value =
                    _uiState.value.copy(processingProgress = "Creating processing record...")
                val history =
                    createTryOnHistory(identityId, selectedUserPhoto, garmentUploadResult.key)

                // 3. Start virtual try-on and poll for completion
                _uiState.value =
                    _uiState.value.copy(processingProgress = "Starting AI processing... This may take 1-2 minutes.")

                Log.d(TAG, "Starting virtual try-on with database polling approach...")

                // Trigger Lambda and poll database for completion
                val tryOnResult = VirtualTryOnService.performVirtualTryOn(
                    userPhotoId = selectedUserPhoto.id,
                    userPhotoUrl = selectedUserPhoto.photoUrl,
                    garmentPhotoUrl = garmentUploadResult.url,
                    historyId = history.id,
                    garmentClass = currentState.selectedGarmentClass.name,
                    mergeStyle = currentState.selectedMergeStyle.name
                )

                // 4. Handle the result
                if (tryOnResult.virtualTryOn.success) {
                    _uiState.value =
                        _uiState.value.copy(processingProgress = "Processing completed!")

                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        processingProgress = ""
                    )

                    Log.i(TAG, "✅ Virtual try-on completed successfully")
                    Log.i(TAG, "Result URL: ${tryOnResult.virtualTryOn.resultUrl}")

                    onComplete(history.id)
                } else {
                    // Update history with error
                    val errorMessage = tryOnResult.virtualTryOn.errorMessage ?: "Processing failed"
                    updateHistoryWithError(history.id, errorMessage)
                    throw Exception(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Virtual try-on failed", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    processingProgress = "",
                    errorMessage = "Failed to process virtual try-on: ${e.message}"
                )
                onComplete(null)
            }
        }
    }

    // Helper method to get TryOnHistory record
    private suspend fun getTryOnHistory(historyId: String): TryOnHistory? =
        suspendCancellableCoroutine { continuation ->
            Amplify.API.query(
                ModelQuery.get(TryOnHistory::class.java, historyId),
                { response ->
                    continuation.resume(response.data)
                },
                { error ->
                    Log.e(TAG, "Failed to get TryOnHistory", error)
                    continuation.resume(null)
                }
            )
        }

    private fun loadUserPhotos() {
        viewModelScope.launch {
            try {
                // Use userId for querying UserPhoto records (not identityId)
                val userId = VirtualTryOnService.getUserId()
                val identityId = VirtualTryOnService.getIdentityId()
                Log.d(TAG, "Loading user photos for userId: $userId")
                Log.d(TAG, "Using identityId for S3 operations: $identityId")

                val userPhotos = suspendCancellableCoroutine { continuation ->
                    Amplify.API.query(
                        ModelQuery.list(UserPhoto::class.java, UserPhoto.USER_ID.eq(userId)),
                        { response ->
                            val photos = response.data?.items?.toList() ?: emptyList()
                            Log.d(TAG, "Found ${photos.size} user photos")
                            photos.forEach { photo ->
                                Log.d(TAG, "Photo: id=${photo.id}, photoUrl=${photo.photoUrl}")
                            }
                            continuation.resume(photos)
                        },
                        { error ->
                            Log.e(TAG, "Failed to load user photos", error)
                            continuation.resume(emptyList())
                        }
                    )
                }

                // Convert S3 keys to presigned URLs if needed
                val photosWithUrls = userPhotos.map { photo ->
                    if (photo.photoUrl.startsWith("http")) {
                        // Already a full URL
                        photo
                    } else {
                        // It's an S3 key, need to generate URL using identityId
                        try {
                            val presignedUrl = generatePresignedUrl(photo.photoUrl, identityId)
                            // Create a new UserPhoto with the presigned URL
                            photo.copyOfBuilder()
                                .photoUrl(presignedUrl)
                                .build()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to generate presigned URL for ${photo.photoUrl}", e)
                            photo // Return original if URL generation fails
                        }
                    }
                }

                // Set default photo as selected if available
                val defaultPhoto = photosWithUrls.firstOrNull { it.isDefault == true }
                    ?: photosWithUrls.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    userPhotos = photosWithUrls,
                    selectedUserPhoto = defaultPhoto
                )

                Log.d(TAG, "Successfully loaded ${photosWithUrls.size} user photos")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user photos", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load user photos: ${e.message}"
                )
            }
        }
    }

    private suspend fun generatePresignedUrl(s3Key: String, identityId: String): String =
        suspendCancellableCoroutine { continuation ->
            Amplify.Storage.getUrl(
                StoragePath.fromIdentityId { identityId ->
                    s3Key
                },
                { result ->
                    val url = result.url.toString()
                    Log.d(TAG, "Generated presigned URL for $s3Key: $url")
                    continuation.resume(url)
                },
                { error ->
                    Log.e(TAG, "Failed to generate presigned URL for $s3Key", error)
                    continuation.resumeWithException(error)
                }
            )
        }

    // Data class to hold upload result with both key and URL
    private data class UploadResult(
        val key: String,
        val url: String,
    )

    private suspend fun uploadGarmentPhoto(
        context: Context,
        file: File,
        identityId: String,
    ): UploadResult =
        suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                try {
                    val timestamp = System.currentTimeMillis()
                    val key = "garment-photos/$identityId/garment-$timestamp.jpg"

                    // Step 1: Compress the image before uploading
                    Log.d(TAG, "Starting image compression...")
                    _uiState.value =
                        _uiState.value.copy(processingProgress = "Compressing image...")

                    val compressedFile = File(file.parent, "compressed_${file.name}")

                    try {
                        ImageCompressionUtils.compressImageFile(
                            context = context,
                            sourceFile = file,
                            targetFile = compressedFile
                        )

                        Log.d(TAG, "Image compression completed")
                        Log.d(
                            TAG,
                            "Original size: ${ImageCompressionUtils.getFileSizeString(file)}"
                        )
                        Log.d(
                            TAG,
                            "Compressed size: ${
                                ImageCompressionUtils.getFileSizeString(compressedFile)
                            }"
                        )

                    } catch (compressionError: Exception) {
                        Log.w(
                            TAG,
                            "Image compression failed, using original file",
                            compressionError
                        )
                        // If compression fails, use the original file
                        file.copyTo(compressedFile, overwrite = true)
                    }

                    // Step 2: Upload the compressed image
                    _uiState.value =
                        _uiState.value.copy(processingProgress = "Uploading compressed image...")

                    val options = StorageUploadFileOptions.builder().build()

                    Amplify.Storage.uploadFile(
                        StoragePath.fromIdentityId { identityId ->
                            key
                        },
                        compressedFile, // Upload the compressed file instead of original
                        options,
                        { progress ->
                            // Update progress if needed
                            val percent =
                                ((progress.currentBytes.toFloat() / progress.totalBytes) * 100).toInt()
                            _uiState.value = _uiState.value.copy(
                                processingProgress = "Uploading compressed image... $percent%"
                            )
                        },
                        { result ->
                            Log.d(TAG, "Garment upload completed")

                            // Clean up compressed file
                            try {
                                compressedFile.delete()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to delete compressed file", e)
                            }

                            // Get the S3 URL for the uploaded file
                            Amplify.Storage.getUrl(
                                StoragePath.fromIdentityId { identityId ->
                                    key
                                },
                                { urlResult ->
                                    val s3Url = urlResult.url.toString()
                                    Log.d(TAG, "Generated S3 URL: $s3Url")
                                    continuation.resume(UploadResult(key, s3Url))
                                },
                                { urlError ->
                                    Log.e(TAG, "Failed to get S3 URL", urlError)
                                    continuation.resumeWithException(urlError)
                                }
                            )
                        },
                        { error ->
                            // Clean up compressed file on error
                            try {
                                compressedFile.delete()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to delete compressed file", e)
                            }

                            Log.e(TAG, "Garment upload failed", error)
                            continuation.resumeWithException(error)
                        }
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "Upload process failed", e)
                    continuation.resumeWithException(e)
                }
            }
        }

    private suspend fun createTryOnHistory(
        identityId: String,
        userPhoto: UserPhoto,
        garmentKey: String,
    ): TryOnHistory = suspendCancellableCoroutine { continuation ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val currentDateString = dateFormat.format(Date())

        val history = TryOnHistory.builder()
            .userId(identityId)
            .userPhotoId(userPhoto.id)
            .userPhotoUrl(userPhoto.photoUrl)
            .garmentPhotoUrl(garmentKey)
            .status(TryOnHistoryStatus.PROCESSING)
            .build()

        Amplify.API.mutate(
            ModelMutation.create(history),
            { response ->
                Log.d(TAG, "TryOnHistory created: ${response.data}")
                continuation.resume(response.data)
            },
            { error ->
                Log.e(TAG, "Failed to create TryOnHistory", error)
                continuation.resumeWithException(error)
            }
        )
    }

    private suspend fun updateHistoryWithResult(
        historyId: String,
        resultUrl: String?,
    ) = suspendCancellableCoroutine { continuation ->
        // First get the existing history record
        Amplify.API.query(
            ModelQuery.get(TryOnHistory::class.java, historyId),
            { queryResponse ->
                val existingHistory = queryResponse.data
                if (existingHistory != null) {
                    // Create updated history with result
                    val updatedHistory = existingHistory.copyOfBuilder()
                        .status(TryOnHistoryStatus.COMPLETED)
                        .resultPhotoUrl(resultUrl)
                        .build()

                    Amplify.API.mutate(
                        ModelMutation.update(updatedHistory),
                        { updateResponse ->
                            Log.d(TAG, "History updated with result: ${updateResponse.data}")
                            continuation.resume(updateResponse.data)
                        },
                        { error ->
                            Log.e(TAG, "Failed to update history with result", error)
                            continuation.resumeWithException(error)
                        }
                    )
                } else {
                    continuation.resumeWithException(Exception("History record not found"))
                }
            },
            { error ->
                Log.e(TAG, "Failed to query history for update", error)
                continuation.resumeWithException(error)
            }
        )
    }

    private suspend fun updateHistoryWithError(
        historyId: String,
        errorMessage: String?,
    ) = suspendCancellableCoroutine { continuation ->
        // First get the existing history record
        Amplify.API.query(
            ModelQuery.get(TryOnHistory::class.java, historyId),
            { queryResponse ->
                val existingHistory = queryResponse.data
                if (existingHistory != null) {
                    // Create updated history with error
                    val updatedHistory = existingHistory.copyOfBuilder()
                        .status(TryOnHistoryStatus.FAILED)
                        .errorMessage(errorMessage)
                        .build()

                    Amplify.API.mutate(
                        ModelMutation.update(updatedHistory),
                        { updateResponse ->
                            Log.d(TAG, "History updated with error: ${updateResponse.data}")
                            continuation.resume(updateResponse.data)
                        },
                        { error ->
                            Log.e(TAG, "Failed to update history with error", error)
                            continuation.resumeWithException(error)
                        }
                    )
                } else {
                    continuation.resumeWithException(Exception("History record not found"))
                }
            },
            { error ->
                Log.e(TAG, "Failed to query history for error update", error)
                continuation.resumeWithException(error)
            }
        )
    }

    fun signOut(onSignOutComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out process...")

                val options = AuthSignOutOptions.builder()
                    .globalSignOut(true)
                    .build()

                val signOutResult = Amplify.Auth.signOut(options) { result ->
                    // Dispatch the callback to the main thread using viewModelScope
                    viewModelScope.launch(Dispatchers.Main) {
                        when (result) {
                            is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                                // Sign Out completed fully and without errors
                                Log.i(TAG, "Signed out successfully")
                                onSignOutComplete(true)
                            }

                            is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                                // Sign Out completed with some errors. User is signed out of the device.
                                Log.w(TAG, "Partial sign out completed")

                                result.hostedUIError?.let {
                                    Log.e(TAG, "HostedUI Error", it.exception)
                                    // Optional: Re-launch it.url in a Custom tab to clear Cognito web session.
                                }
                                result.globalSignOutError?.let {
                                    Log.e(TAG, "GlobalSignOut Error", it.exception)
                                    // Optional: Use escape hatch to retry revocation of it.accessToken.
                                }
                                result.revokeTokenError?.let {
                                    Log.e(TAG, "RevokeToken Error", it.exception)
                                    // Optional: Use escape hatch to retry revocation of it.refreshToken.
                                }

                                // Even with partial errors, user is signed out of the device
                                onSignOutComplete(true)
                            }

                            is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                                // Sign Out failed with an exception, leaving the user signed in.
                                Log.e(TAG, "Sign out failed", result.exception)
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "Sign out failed: ${result.exception.message}"
                                )
                                onSignOutComplete(false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during sign out", e)
                // Ensure this is also on the main thread
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Sign out failed: ${e.message}"
                    )
                    onSignOutComplete(false)
                }
            }
        }
    }

    fun uploadUserPhoto(context: Context, imageUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting user photo upload...")

                val identityId = VirtualTryOnService.getIdentityId()
                val userId = VirtualTryOnService.getUserId()

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
                try {
                    ImageCompressionUtils.compressImageFromUri(
                        context = context,
                        uri = imageUri,
                        targetFile = compressedFile
                    )
                    Log.d(TAG, "Image compression completed")
                } catch (compressionError: Exception) {
                    Log.e(TAG, "Image compression from URI failed", compressionError)
                    throw compressionError
                }

                // Upload to S3 using compressed file
                val uploadedKey = uploadUserPhotoToS3(key, compressedFile)

                // Clean up compressed file
                try {
                    compressedFile.delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete compressed file", e)
                }

                // Create database record
                createUserPhotoRecord(userId, uploadedKey)

                // Reload user photos to refresh the list
                loadUserPhotos()

                Log.i(TAG, "User photo uploaded successfully")
                onComplete(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload user photo", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to upload photo: ${e.message}"
                )
                onComplete(false)
            }
        }
    }

    private suspend fun uploadUserPhotoToS3(
        key: String,
        file: File,
    ): String = suspendCancellableCoroutine { continuation ->
        val options = StorageUploadFileOptions.builder().build()

        Amplify.Storage.uploadFile(
            StoragePath.fromIdentityId { identityId ->
                key
            },
            file,
            options,
            { progress ->
                // Could add progress tracking here if needed
                Log.d(TAG, "Upload progress: ${progress.fractionCompleted}")
            },
            { result ->
                Log.d(TAG, "User photo upload completed successfully")
                continuation.resume(key)
            },
            { error ->
                Log.e(TAG, "User photo upload failed", error)
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
        private const val TAG = "HomeViewModel"
    }
}

