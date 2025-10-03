package id.harissabil.wearnow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.harissabil.wearnow.ui.navigation.NavGraph
import id.harissabil.wearnow.ui.theme.WearnowandroidTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.splashCondition
            }
        }
        enableEdgeToEdge()
        setContent {
            WearnowandroidTheme {
                val startDestination by viewModel.startDestination.collectAsState()
                NavGraph(
                    startDestination = startDestination
                )
            }
        }
    }
}