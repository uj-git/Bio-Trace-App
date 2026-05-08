package com.umang.biotrace.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun AppNavGraph(
    navController: NavHostController,
    permissionsGranted: Boolean
) {

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {

        composable(AppRoute.Splash.route) {

            SplashScreen(
                permissionsGranted = permissionsGranted,
                onNavigateToHome = {

                    navController.navigate(AppRoute.Home.route) {

                        popUpTo(AppRoute.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.Home.route) {

            HomeScreen(
                onPalmScanClick = {
                    navController.navigate(AppRoute.PalmDetection.route)
                }
            )
        }

        composable(AppRoute.PalmDetection.route) {

            PalmDetectionScreen(
                onPalmCaptured = {
                    navController.navigate(AppRoute.FingerDetection.route)
                }
            )
        }

        composable(AppRoute.FingerDetection.route) {

            FingerDetectionScreen(
                onCompleted = {
                    navController.navigate(AppRoute.Result.route)
                }
            )
        }

        composable(AppRoute.Result.route) {

            ResultScreen()
        }
    }
}