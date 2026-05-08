package com.umang.biotrace.presentation.navigation

sealed class AppRoute(val route: String) {

    data object Splash : AppRoute("splash")

    data object Home : AppRoute("home")

    data object PalmDetection : AppRoute("palm_detection")

    data object FingerDetection : AppRoute("finger_detection")

    data object Result : AppRoute("result")
}