package id.harissabil.wearnow.ui.screen.history

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.TryOnHistory
import com.amplifyframework.datastore.generated.model.TryOnHistoryStatus
import com.amplifyframework.storage.StoragePath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class HistoryItemUiState(
    val history: TryOnHistory,
    val resultImageUri: Uri? = null,
    val isLoadingImage: Boolean = false
)

data class HistoryUiState(
    val historyItems: List<HistoryItemUiState> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filterStatus: TryOnHistoryStatus? = null
)

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                // Get identityId for querying (not userId!)
                // TryOnHistory.userId field actually contains identityId
                val identityId = getIdentityId()

                Log.d(TAG, "Loading history for identityId: $identityId")

                // Query all TryOnHistory records for this identity
                val histories = fetchTryOnHistories(identityId)

                Log.d(TAG, "Found ${histories.size} history records")

                // Convert to UI state with image loading
                val historyItems = histories.map { history ->
                    HistoryItemUiState(
                        history = history,
                        isLoadingImage = true
                    )
                }

                _uiState.value = _uiState.value.copy(
                    historyItems = historyItems,
                    isLoading = false
                )

                // Load images for each item
                historyItems.forEach { item ->
                    loadImageForHistoryItem(item, identityId)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load history", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load history: ${e.message}"
                )
            }
        }
    }

    fun filterByStatus(status: TryOnHistoryStatus?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun getUserId(): String = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoAuthSession = result as AWSCognitoAuthSession
                val userId = cognitoAuthSession.userSubResult.value
                continuation.resume(userId as String)
            },
            { error ->
                Log.e(TAG, "Failed to get userId", error)
                continuation.resumeWithException(error)
            }
        )
    }

    private suspend fun getIdentityId(): String = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoAuthSession = result as AWSCognitoAuthSession
                val identityId = cognitoAuthSession.identityIdResult.value
                continuation.resume(identityId as String)
            },
            { error ->
                Log.e(TAG, "Failed to get identityId", error)
                continuation.resumeWithException(error)
            }
        )
    }

    private suspend fun fetchTryOnHistories(identityId: String): List<TryOnHistory> =
        suspendCancellableCoroutine { continuation ->
            Amplify.API.query(
                ModelQuery.list(TryOnHistory::class.java, TryOnHistory.USER_ID.eq(identityId)),
                { response ->
                    val histories = response.data?.items?.toList()?.sortedByDescending {
                        it.createdAt.toDate()
                    } ?: emptyList()
                    continuation.resume(histories)
                },
                { error ->
                    Log.e(TAG, "Failed to fetch TryOnHistory", error)
                    continuation.resumeWithException(error)
                }
            )
        }

    private fun loadImageForHistoryItem(item: HistoryItemUiState, identityId: String) {
        viewModelScope.launch {
            try {
                // Only load image if status is COMPLETED and resultPhotoUrl exists
                if (item.history.status == TryOnHistoryStatus.COMPLETED &&
                    !item.history.resultPhotoUrl.isNullOrEmpty()) {

                    val imageUri = if (item.history.resultPhotoUrl.startsWith("http")) {
                        item.history.resultPhotoUrl.toUri()
                    } else {
                        generatePresignedUrl(item.history.resultPhotoUrl, identityId)
                    }

                    // Update the specific item in the list
                    val updatedItems = _uiState.value.historyItems.map {
                        if (it.history.id == item.history.id) {
                            it.copy(
                                resultImageUri = imageUri,
                                isLoadingImage = false
                            )
                        } else {
                            it
                        }
                    }

                    _uiState.value = _uiState.value.copy(historyItems = updatedItems)
                } else {
                    // Mark as not loading for non-completed items
                    val updatedItems = _uiState.value.historyItems.map {
                        if (it.history.id == item.history.id) {
                            it.copy(isLoadingImage = false)
                        } else {
                            it
                        }
                    }
                    _uiState.value = _uiState.value.copy(historyItems = updatedItems)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image for ${item.history.id}", e)
                // Mark as not loading even on error
                val updatedItems = _uiState.value.historyItems.map {
                    if (it.history.id == item.history.id) {
                        it.copy(isLoadingImage = false)
                    } else {
                        it
                    }
                }
                _uiState.value = _uiState.value.copy(historyItems = updatedItems)
            }
        }
    }

    private suspend fun generatePresignedUrl(s3Key: String, identityId: String): Uri =
        suspendCancellableCoroutine { continuation ->
            Amplify.Storage.getUrl(
                StoragePath.fromIdentityId { identityId ->
                    s3Key
                },
                { result ->
                    continuation.resume(result.url.toString().toUri())
                },
                { error ->
                    Log.e(TAG, "Failed to generate presigned URL for $s3Key", error)
                    continuation.resumeWithException(error)
                }
            )
        }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}