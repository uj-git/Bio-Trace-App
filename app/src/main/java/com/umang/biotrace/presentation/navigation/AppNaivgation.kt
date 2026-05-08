package com.umang.biotrace.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.umang.biotrace.presentation.fingerdetection.FingerDetectionScreen
import com.umang.biotrace.presentation.homescreen.HomeScreen
import com.umang.biotrace.presentation.palmdetection.PalmDetectionScreen
import com.umang.biotrace.presentation.capture.CaptureViewModel
import com.umang.biotrace.presentation.resultscreen.ResultScreen
import com.umang.biotrace.presentation.splashscreen.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    permissionsGranted: Boolean
) {
    val captureViewModel: CaptureViewModel = koinViewModel()

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
                viewModel = captureViewModel,
                onPalmCaptured = {
                    navController.navigate(AppRoute.FingerDetection.route)
                }
            )
        }

        composable(AppRoute.FingerDetection.route) {

            FingerDetectionScreen(
                viewModel = captureViewModel,
                onCompleted = {
                    navController.navigate(AppRoute.Result.route)
                }
            )
        }

        composable(AppRoute.Result.route) {

            ResultScreen(
                viewModel = captureViewModel,
                onFinish = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
