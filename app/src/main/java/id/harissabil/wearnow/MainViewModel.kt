package id.harissabil.wearnow

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.UserPhoto
import id.harissabil.wearnow.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainViewModel : ViewModel() {

    var splashCondition by mutableStateOf(true)
        private set

    private val _startDestination = MutableStateFlow<Route>(Route.Splash)
    val startDestination: StateFlow<Route> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            determineStartDestination()

            // Temporary hardcoded start destination for testing
//            _startDestination.value = Route.Onboarding
//            splashCondition = false
        }
    }

    private suspend fun determineStartDestination() {
        try {
            val isSignedIn = checkIfUserSignedIn()

            if (!isSignedIn) {
                _startDestination.value = Route.Auth
            } else {
                val hasUploadedPhoto = checkIfUserHasPhoto()
                _startDestination.value = if (hasUploadedPhoto) {
                    Route.Home
                } else {
                    Route.Onboarding
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error determining start destination: ${e.message}")
            _startDestination.value = Route.Auth
        } finally {
            splashCondition = false
        }
    }

    private suspend fun checkIfUserSignedIn(): Boolean =
        suspendCancellableCoroutine { continuation ->
            try {
                Amplify.Auth.fetchAuthSession(
                    { result -> continuation.resume(result.isSignedIn) },
                    { error -> continuation.resumeWithException(error) }
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    suspend fun checkIfUserHasPhoto(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            // Get current user ID
            Amplify.Auth.getCurrentUser(
                { user ->
                    val userId = user.userId

                    // Query UserPhoto for this user
                    Amplify.API.query(
                        ModelQuery.list(
                            UserPhoto::class.java,
                            UserPhoto.USER_ID.eq(userId)
                        ),
                        { response ->
                            // Check if any photos exist
                            val hasPhotos = response.data.items.iterator().hasNext()
                            continuation.resume(hasPhotos)
                        },
                        { error ->
                            Log.e(TAG, "Failed to query user photos: $error")
                            continuation.resumeWithException(error)
                        }
                    )
                },
                { error ->
                    Log.e(TAG, "Failed to get current user: $error")
                    continuation.resumeWithException(error)
                }
            )
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}