package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Water : Screen("water")
    object Trophy : Screen("trophy")
    object Stats : Screen("stats")
    object Habits : Screen("habits")
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}
