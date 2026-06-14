package dev.chrisotm.barbelltracker.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisotm.barbelltracker.R

private data class TopLevel(
    val route: String,
    val titleRes: Int,
    val labelRes: Int,
    val icon: ImageVector,
)

/**
 * App shell with a persistent bottom [NavigationBar] for the top-level sections. The bar and a
 * matching [TopAppBar] are shown only on top-level routes; detail / full-screen routes get no
 * chrome here and supply their own [Scaffold] (with a back arrow). The create FAB appears only on
 * the routes where "add" makes sense (Plans, Exercises).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit,
) {
    val tabs = listOf(
        TopLevel(Routes.PLANS, R.string.app_name, R.string.nav_plans, Icons.AutoMirrored.Filled.List),
        TopLevel(Routes.EXERCISES, R.string.exercises_title, R.string.nav_exercises, Icons.Filled.FitnessCenter),
        TopLevel(Routes.HISTORY, R.string.history_title, R.string.nav_history, Icons.Filled.DateRange),
        TopLevel(Routes.STATISTICS, R.string.statistics_title, R.string.nav_statistics, Icons.Filled.BarChart),
        TopLevel(Routes.SETTINGS, R.string.settings_title, R.string.nav_settings, Icons.Filled.Settings),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val current = tabs.firstOrNull { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (current != null) {
                TopAppBar(title = { Text(stringResource(current.titleRes)) })
            }
        },
        bottomBar = {
            if (current != null) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                Routes.PLANS -> FloatingActionButton(
                    onClick = { navController.navigate(Routes.PLAN_CREATE) },
                ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.create_plan)) }

                Routes.EXERCISES -> FloatingActionButton(
                    onClick = { navController.navigate(Routes.exerciseEdit(0)) },
                ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_exercise)) }
            }
        },
    ) { padding ->
        content(Modifier.padding(padding))
    }
}
