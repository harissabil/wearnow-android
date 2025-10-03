package id.harissabil.wearnow.ui.screen.home.utils

import android.util.Log
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.google.gson.Gson
import id.harissabil.wearnow.ui.screen.home.data.VirtualTryOnResponse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Direct Lambda invocation service using proper GraphQL mutation
 * This calls your virtual-tryon Lambda function through GraphQL
 */
object DirectLambdaService {

    private const val TAG = "DirectLambdaService"

    // Helper function to get identityId (same as your S3 uploads)
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

    // Helper function to get userId from Cognito User Pool
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

    // Perform virtual try-on using GraphQL mutation (following Amplify documentation)
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun performVirtualTryOn(
        userPhotoId: String,
        userPhotoUrl: String,
        garmentPhotoUrl: String,
        historyId: String,
        garmentClass: String = "UPPER_BODY",
        mergeStyle: String = "BALANCED"
    ): VirtualTryOnResponse = suspendCancellableCoroutine { continuation ->

        // Create coroutine scope to get identityId first
        kotlinx.coroutines.GlobalScope.launch {
            try {
                // Get the identityId (matches S3 storage pattern for Lambda)
                val userId = getIdentityId()

                // GraphQL mutation document - this calls your Lambda function
                val document = """
                    mutation VirtualTryOnMutation(
                        ${'$'}userId: String!,
                        ${'$'}userPhotoId: String!,
                        ${'$'}userPhotoUrl: String!,
                        ${'$'}garmentPhotoUrl: String!,
                        ${'$'}historyId: String!,
                        ${'$'}garmentClass: String!,
                        ${'$'}mergeStyle: String!
                    ) {
                        virtualTryOn(
                            userId: ${'$'}userId,
                            userPhotoId: ${'$'}userPhotoId,
                            userPhotoUrl: ${'$'}userPhotoUrl,
                            garmentPhotoUrl: ${'$'}garmentPhotoUrl,
                            historyId: ${'$'}historyId,
                            garmentClass: ${'$'}garmentClass,
                            mergeStyle: ${'$'}mergeStyle
                        ) {
                            success
                            historyId
                            resultUrl
                            errorMessage
                            processingTime
                        }
                    }
                """.trimIndent()

                // Variables for the mutation
                val variables = mapOf(
                    "userId" to userId,
                    "userPhotoId" to userPhotoId,
                    "userPhotoUrl" to userPhotoUrl,
                    "garmentPhotoUrl" to garmentPhotoUrl,
                    "historyId" to historyId,
                    "garmentClass" to garmentClass,
                    "mergeStyle" to mergeStyle
                )

                Log.d(TAG, "Executing GraphQL mutation with variables: $variables")

                // Create the GraphQL request
                val virtualTryOnRequest = SimpleGraphQLRequest<String>(
                    document,
                    variables,
                    String::class.java,
                    GsonVariablesSerializer()
                )

                // Execute the mutation using Amplify.API.mutate (following the documentation pattern)
                Amplify.API.mutate(
                    virtualTryOnRequest,
                    { response ->
                        try {
                            Log.d(TAG, "GraphQL mutation response: ${response.data}")

                            if (response.data == null) {
                                Log.e(TAG, "GraphQL response data is null - mutation may not be defined in schema")
                                continuation.resumeWithException(
                                    Exception("GraphQL mutation returned null - check if virtualTryOn mutation exists in schema")
                                )
                                return@mutate
                            }

                            val gson = Gson()
                            val result = gson.fromJson(response.data, VirtualTryOnResponse::class.java)

                            if (result == null) {
                                Log.e(TAG, "Failed to parse GraphQL response - response was null after JSON parsing")
                                continuation.resumeWithException(
                                    Exception("Failed to parse GraphQL response - null result")
                                )
                                return@mutate
                            }

                            if (result.virtualTryOn.success) {
                                Log.i(TAG, "GraphQL mutation try-on completed: ${result.virtualTryOn.resultUrl}")
                                Log.i(TAG, "Processing time: ${result.virtualTryOn.processingTime}ms")
                            } else {
                                Log.e(TAG, "GraphQL mutation try-on failed: ${result.virtualTryOn.errorMessage}")
                            }

                            continuation.resume(result)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse GraphQL mutation response", e)
                            Log.e(TAG, "Response data was: ${response.data}")
                            continuation.resumeWithException(e)
                        }
                    },
                    { error ->
                        Log.e(TAG, "GraphQL mutation failed", error)
                        Log.e(TAG, "Error details: ${error.localizedMessage}")
                        Log.e(TAG, "This likely means the 'virtualTryOn' mutation is not defined in your GraphQL schema")
                        continuation.resumeWithException(error)
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare GraphQL mutation", e)
                continuation.resumeWithException(e)
            }
        }
    }
}
