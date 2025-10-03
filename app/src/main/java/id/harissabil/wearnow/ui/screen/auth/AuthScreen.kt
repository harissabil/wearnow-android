package id.harissabil.wearnow.ui.screen.auth

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amplifyframework.ui.authenticator.ui.Authenticator
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onGoToHome: () -> Unit,
    onGoToOnboarding: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val requestMultiplePermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permission ->
        if (permission) {
            // All permissions are granted
        } else {
            // Some permissions are denied
            Log.d("AuthScreen", "Some permissions are denied.")
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState
                    .showSnackbar(
                        message = "Please enable location services to continue.",
                        actionLabel = "Enable",
                        duration = SnackbarDuration.Long
                    )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }

                    SnackbarResult.Dismissed -> {
                        Log.d("AuthScreen", "Snackbar dismissed")
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        requestMultiplePermissions.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Authenticator(
                modifier = Modifier.padding(horizontal = 16.dp),
                content = {
                    LaunchedEffect(Unit) {
                        scope.launch {
                            if (viewModel.checkIfUserHasPhoto()) {
                                onGoToHome()
                            } else {
                                onGoToOnboarding()
                            }
                        }
                    }
                }
            )
        }
    }
}