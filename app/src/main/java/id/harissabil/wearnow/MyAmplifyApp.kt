package id.harissabil.wearnow

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

class MyAmplifyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(
                AmplifyOutputs.fromResource(R.raw.amplify_outputs),
                applicationContext
            )
            Log.i(TAG, "Amplify initialized successfully")

            // Verify plugins are loaded
            Log.i(TAG, "Auth plugin: ${Amplify.Auth.plugins}")
            Log.i(TAG, "API plugin: ${Amplify.API.plugins}")
            Log.i(TAG, "Storage plugin: ${Amplify.Storage.plugins}")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
    }

    companion object {
        private const val TAG = "WearNowApplication"
    }
}