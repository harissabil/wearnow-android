package id.harissabil.wearnow.ui.screen.result

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.TryOnHistory
import com.amplifyframework.storage.StoragePath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ResultViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadResult(historyId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                // Fetch try-on history
                val history = fetchTryOnHistory(historyId)

                if (history == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Try-on result not found"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    tryOnHistory = history,
                    isLoading = false
                )

                // Generate presigned URLs for display
                generatePresignedUrls(history)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load result", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load result: ${e.message}"
                )
            }
        }
    }

    fun retryDownload() {
        val history = _uiState.value.tryOnHistory
        if (history != null) {
            generatePresignedUrls(history)
        }
    }

    fun showShareDialog() {
        _uiState.value = _uiState.value.copy(showShareDialog = true)
    }

    fun hideShareDialog() {
        _uiState.value = _uiState.value.copy(showShareDialog = false)
    }

    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun deleteResult(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                val history = _uiState.value.tryOnHistory
                if (history == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No result to delete"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                Log.d(TAG, "Deleting TryOnHistory record: ${history.id}")

                // Delete the TryOnHistory record from database
                deleteTryOnHistory(history)

                Log.i(TAG, "✅ TryOnHistory deleted successfully: ${history.id}")

                _uiState.value = _uiState.value.copy(isLoading = false)

                // Hide the delete dialog
                hideDeleteDialog()

                // Call the callback to navigate back
                onDeleted()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to delete result", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to delete result: ${e.message}"
                )
                hideDeleteDialog()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun fetchTryOnHistory(historyId: String): TryOnHistory? =
        suspendCancellableCoroutine { continuation ->
            Amplify.API.query(
                ModelQuery.get(TryOnHistory::class.java, historyId),
                { response ->
                    Log.d(TAG, "TryOnHistory fetched: ${response.data}")
                    continuation.resume(response.data)
                },
                { error ->
                    Log.e(TAG, "Failed to fetch TryOnHistory", error)
                    continuation.resumeWithException(error)
                }
            )
        }

    private fun generatePresignedUrls(history: TryOnHistory) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isDownloading = true,
                    downloadProgress = "Loading images..."
                )

                // Get identity ID for S3 operations
                val identityId = getIdentityId()

                // User photo URL - check if it's already a presigned URL or S3 key
                _uiState.value = _uiState.value.copy(downloadProgress = "Loading your photo...")
                val userPhotoUri = if (history.userPhotoUrl.startsWith("http")) {
                    // Already a presigned URL, use it directly
                    Log.d(TAG, "User photo is already a presigned URL")
                    history.userPhotoUrl.toUri()
                } else {
                    // It's an S3 key, generate presigned URL
                    Log.d(TAG, "Generating presigned URL for user photo: ${history.userPhotoUrl}")
                    generatePresignedUrl(history.userPhotoUrl, identityId)
                }

                // Garment photo - should be an S3 key
                _uiState.value = _uiState.value.copy(downloadProgress = "Loading garment photo...")
                val garmentPhotoUri = if (history.garmentPhotoUrl.startsWith("http")) {
                    history.garmentPhotoUrl.toUri()
                } else {
                    Log.d(TAG, "Generating presigned URL for garment: ${history.garmentPhotoUrl}")
                    generatePresignedUrl(history.garmentPhotoUrl, identityId)
                }

                // Result photo - should be an S3 key
                var resultPhotoUri: Uri? = null
                if (!history.resultPhotoUrl.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(downloadProgress = "Loading result...")
                    resultPhotoUri = if (history.resultPhotoUrl.startsWith("http")) {
                        history.resultPhotoUrl.toUri()
                    } else {
                        Log.d(TAG, "Generating presigned URL for result: ${history.resultPhotoUrl}")
                        generatePresignedUrl(history.resultPhotoUrl, identityId)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    userPhotoUri = userPhotoUri,
                    garmentPhotoUri = garmentPhotoUri,
                    resultPhotoUri = resultPhotoUri,
                    isDownloading = false,
                    downloadProgress = ""
                )

                Log.i(TAG, "✅ All images loaded successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate presigned URLs", e)
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    downloadProgress = "",
                    errorMessage = "Failed to load images: ${e.message}"
                )
            }
        }
    }

    private suspend fun getIdentityId(): String = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoAuthSession = result as AWSCognitoAuthSession
                val identityId = cognitoAuthSession.identityIdResult.value
                Log.d(TAG, "Retrieved identityId: $identityId")
                continuation.resume(identityId as String)
            },
            { error ->
                Log.e(TAG, "Failed to get identityId", error)
                continuation.resumeWithException(error)
            }
        )
    }

    private suspend fun generatePresignedUrl(s3Key: String, identityId: String): Uri =
        suspendCancellableCoroutine { continuation ->
            Amplify.Storage.getUrl(
                StoragePath.fromIdentityId { identityId ->
                    s3Key
                },
                { result ->
                    val url = result.url.toString()
                    Log.d(TAG, "Generated presigned URL for $s3Key")
                    continuation.resume(url.toUri())
                },
                { error ->
                    Log.e(TAG, "Failed to generate presigned URL for $s3Key", error)
                    continuation.resumeWithException(error)
                }
            )
        }

    private suspend fun deleteTryOnHistory(history: TryOnHistory) =
        suspendCancellableCoroutine { continuation ->
            Amplify.API.mutate(
                com.amplifyframework.api.graphql.model.ModelMutation.delete(history),
                { response ->
                    Log.d(TAG, "TryOnHistory deletion response: ${response.data}")
                    continuation.resume(Unit)
                },
                { error ->
                    Log.e(TAG, "Failed to delete TryOnHistory", error)
                    continuation.resumeWithException(error)
                }
            )
        }

    companion object {
        private const val TAG = "ResultViewModel"
    }
}