package com.example.lichvannien

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.ui.MainScreen
import com.example.lichvannien.ui.detail.DayDetailScreen
import com.example.lichvannien.ui.navigation.Screen
import com.example.lichvannien.ui.onboarding.OnboardingScreen

@Composable
fun MainNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    
    // Lắng nghe ngày sinh từ DataStore
    val birthdayState by userPreferences.birthdayFlow.collectAsStateWithLifecycle(initialValue = null)

    Box(modifier = Modifier.fillMaxSize()) {
        val state = birthdayState
        if (state == null) {
            // Màn hình Splash/Loading tối giản để tránh nhấp nháy màn hình
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            val hasCompletedOnboarding = state.first != 0 && state.second != 0
            val startDestination = if (hasCompletedOnboarding) Screen.Calendar.route else Screen.Onboarding.route

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinished = {
                            navController.navigate(Screen.Calendar.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Calendar.route) {
                    MainScreen(navController = navController)
                }

                composable(
                    route = Screen.DayDetail.route,
                    arguments = listOf(
                        navArgument("year") { type = NavType.IntType },
                        navArgument("month") { type = NavType.IntType },
                        navArgument("day") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val year = backStackEntry.arguments?.getInt("year") ?: 0
                    val month = backStackEntry.arguments?.getInt("month") ?: 0
                    val day = backStackEntry.arguments?.getInt("day") ?: 0
                    DayDetailScreen(
                        year = year,
                        month = month,
                        day = day,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
