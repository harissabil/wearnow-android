package id.harissabil.wearnow.ui.screen.home.utils

import android.util.Log
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.TryOnHistory
import com.amplifyframework.datastore.generated.model.TryOnHistoryStatus
import id.harissabil.wearnow.ui.screen.home.data.VirtualTryOnResponse
import id.harissabil.wearnow.ui.screen.home.data.VirtualTryOnResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object VirtualTryOnService {

    private const val TAG = "VirtualTryOnService"
    private const val POLLING_INTERVAL_MS = 5000L // Poll every 5 seconds
    private const val MAX_POLLING_ATTEMPTS = 30 // Poll for up to 2.5 minutes (30 * 5 seconds)

    // Helper function to get identityId
    suspend fun getIdentityId(): String = suspendCancellableCoroutine { continuation ->
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

    // Helper function to get userId
    suspend fun getUserId(): String = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoAuthSession = result as AWSCognitoAuthSession
                val userId = cognitoAuthSession.userSubResult.value
                Log.d(TAG, "Retrieved userId: $userId")
                continuation.resume(userId as String)
            },
            { error ->
                Log.e(TAG, "Failed to get userId", error)
                continuation.resumeWithException(error)
            }
        )
    }

    /**
     * Start virtual try-on and poll for completion
     * This handles the AppSync 30-second timeout by:
     * 1. Starting the Lambda (which will timeout on AppSync but keep running)
     * 2. Polling the database to check when Lambda updates the status
     */
    suspend fun performVirtualTryOn(
        userPhotoId: String,
        userPhotoUrl: String,
        garmentPhotoUrl: String,
        historyId: String,
        garmentClass: String = "UPPER_BODY",
        mergeStyle: String = "BALANCED"
    ): VirtualTryOnResponse {
        // Step 1: Trigger the Lambda (will timeout but Lambda keeps running)
        Log.d(TAG, "Triggering Lambda function (this will timeout but Lambda continues)...")
        triggerVirtualTryOnLambda(
            userPhotoId, userPhotoUrl, garmentPhotoUrl,
            historyId, garmentClass, mergeStyle
        )

        // Step 2: Poll the database for completion
        Log.d(TAG, "Starting database polling to check for completion...")
        return pollDatabaseForCompletion(historyId)
    }

    /**
     * Trigger the Lambda function (will timeout but Lambda keeps running)
     */
    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun triggerVirtualTryOnLambda(
        userPhotoId: String,
        userPhotoUrl: String,
        garmentPhotoUrl: String,
        historyId: String,
        garmentClass: String,
        mergeStyle: String
    ) {
        try {
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val userId = getIdentityId()

                    val document = """
                        mutation VirtualTryOn(
                            ${'$'}userId: String!,
                            ${'$'}userPhotoId: String!,
                            ${'$'}userPhotoUrl: String!,
                            ${'$'}garmentPhotoUrl: String!,
                            ${'$'}historyId: String!,
                            ${'$'}garmentClass: String,
                            ${'$'}maskType: String,
                            ${'$'}mergeStyle: String
                        ) {
                            virtualTryOn(
                                userId: ${'$'}userId,
                                userPhotoId: ${'$'}userPhotoId,
                                userPhotoUrl: ${'$'}userPhotoUrl,
                                garmentPhotoUrl: ${'$'}garmentPhotoUrl,
                                historyId: ${'$'}historyId,
                                garmentClass: ${'$'}garmentClass,
                                maskType: ${'$'}maskType,
                                mergeStyle: ${'$'}mergeStyle
                            )
                        }
                    """.trimIndent()

                    val variables = mapOf(
                        "userId" to userId,
                        "userPhotoId" to userPhotoId,
                        "userPhotoUrl" to userPhotoUrl,
                        "garmentPhotoUrl" to garmentPhotoUrl,
                        "historyId" to historyId,
                        "garmentClass" to garmentClass,
                        "maskType" to "GARMENT",
                        "mergeStyle" to mergeStyle
                    )

                    val request = SimpleGraphQLRequest<String>(
                        document,
                        variables,
                        String::class.java,
                        GsonVariablesSerializer()
                    )

                    Amplify.API.mutate(
                        request,
                        { response ->
                            Log.d(TAG, "Lambda triggered successfully (or completed within 30s)")
                        },
                        { error ->
                            // This timeout is EXPECTED - Lambda will keep running
                            Log.w(TAG, "AppSync timeout (expected) - Lambda is still processing in background")
                        }
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error triggering Lambda (expected timeout)", e)
                }
            }

            // Give Lambda a moment to start
            delay(3000)

        } catch (e: Exception) {
            Log.w(TAG, "Failed to trigger Lambda", e)
        }
    }

    /**
     * Poll the database for completion by checking TryOnHistory status
     */
    private suspend fun pollDatabaseForCompletion(historyId: String): VirtualTryOnResponse {
        var attempts = 0

        while (attempts < MAX_POLLING_ATTEMPTS) {
            try {
                Log.d(TAG, "Polling attempt ${attempts + 1}/$MAX_POLLING_ATTEMPTS")

                val history = getTryOnHistory(historyId)

                when (history?.status) {
                    TryOnHistoryStatus.COMPLETED -> {
                        Log.i(TAG, "✅ Try-on completed successfully!")
                        val result = VirtualTryOnResult(
                            success = true,
                            historyId = historyId,
                            resultUrl = history.resultPhotoUrl,
                            processingTime = 0,
                            errorMessage = null
                        )
                        return VirtualTryOnResponse(result)
                    }
                    TryOnHistoryStatus.FAILED -> {
                        Log.e(TAG, "❌ Try-on failed: ${history.errorMessage}")
                        val result = VirtualTryOnResult(
                            success = false,
                            historyId = historyId,
                            resultUrl = null,
                            processingTime = 0,
                            errorMessage = history.errorMessage ?: "Processing failed"
                        )
                        return VirtualTryOnResponse(result)
                    }
                    TryOnHistoryStatus.PROCESSING -> {
                        Log.d(TAG, "Still processing... waiting ${POLLING_INTERVAL_MS}ms")
                        // Continue polling
                    }
                    null -> {
                        Log.w(TAG, "History record not found yet")
                    }
                }

                attempts++
                if (attempts < MAX_POLLING_ATTEMPTS) {
                    delay(POLLING_INTERVAL_MS)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error polling database", e)
                attempts++
                if (attempts < MAX_POLLING_ATTEMPTS) {
                    delay(POLLING_INTERVAL_MS)
                }
            }
        }

        Log.e(TAG, "⏱️ Polling timeout after $MAX_POLLING_ATTEMPTS attempts")
        throw Exception("Processing timeout - the operation may still be running in the background")
    }

    /**
     * Get TryOnHistory record from database
     */
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
}
