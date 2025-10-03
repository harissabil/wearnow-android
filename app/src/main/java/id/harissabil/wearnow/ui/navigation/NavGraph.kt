package id.harissabil.wearnow.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import id.harissabil.wearnow.ui.screen.auth.AuthScreen
import id.harissabil.wearnow.ui.screen.history.HistoryScreen
import id.harissabil.wearnow.ui.screen.home.HomeScreen
import id.harissabil.wearnow.ui.screen.onboarding.OnboardingScreen
import id.harissabil.wearnow.ui.screen.result.ResultScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    startDestination: Route,
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        NavHost(
            modifier = modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            navController = navController,
            startDestination = startDestination,
        ) {
            composable<Route.Splash> {}

            composable<Route.Auth> {
                AuthScreen(
                    onGoToHome = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                        }
                    },
                    onGoToOnboarding = {
                        navController.navigate(Route.Onboarding) {
                            popUpTo(Route.Onboarding) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Onboarding> {
                OnboardingScreen(
                    onGoToHome = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Home> {
                HomeScreen(
                    onNavigateToResult = { historyId ->
                        navController.navigate(Route.Result(historyId))
                    },
                    onNavigateToHistory = {
                        navController.navigate(Route.History)
                    },
                    onNavigateToAuth = {
                        navController.navigate(Route.Auth) {
                            popUpTo(Route.Auth) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Result> {
                val args = it.toRoute<Route.Result>()
                ResultScreen(
                    historyId = args.historyId,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            composable<Route.History> {
                HistoryScreen(
                    onBack = { navController.navigateUp() },
                    onGoToResult = { historyId ->
                        navController.navigate(Route.Result(historyId))
                    }
                )
            }
        }
    }
}