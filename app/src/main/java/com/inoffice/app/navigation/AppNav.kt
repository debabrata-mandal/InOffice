package com.inoffice.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.inoffice.app.feature.dashboard.DashboardRoute
import com.inoffice.app.feature.report.ReportRoute
import com.inoffice.app.feature.settings.SettingsRoute

object Routes {
    const val Dashboard = "dashboard"
    const val Report = "report"
    const val Settings = "settings"
}

@Composable
fun InOfficeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard,
        modifier = modifier,
    ) {
        composable(Routes.Dashboard) {
            DashboardRoute(
                onOpenReport = { navController.navigate(Routes.Report) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            )
        }
        composable(Routes.Report) {
            ReportRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.Settings) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
