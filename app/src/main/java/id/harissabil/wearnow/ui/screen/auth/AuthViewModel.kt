package id.harissabil.wearnow.ui.screen.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.UserPhoto
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthViewModel : ViewModel() {

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
        private const val TAG = "AuthViewModel"
    }
}