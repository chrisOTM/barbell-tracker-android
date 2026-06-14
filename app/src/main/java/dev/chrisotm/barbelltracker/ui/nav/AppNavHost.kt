package dev.chrisotm.barbelltracker.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.chrisotm.barbelltracker.ui.exercises.ExerciseEditScreen
import dev.chrisotm.barbelltracker.ui.exercises.ExerciseLibraryScreen
import dev.chrisotm.barbelltracker.ui.history.ExerciseProgressScreen
import dev.chrisotm.barbelltracker.ui.history.HistoryListScreen
import dev.chrisotm.barbelltracker.ui.history.SessionDetailScreen
import dev.chrisotm.barbelltracker.ui.plans.PlanCreateScreen
import dev.chrisotm.barbelltracker.ui.plans.PlanEditScreen
import dev.chrisotm.barbelltracker.ui.plans.PlansListScreen
import dev.chrisotm.barbelltracker.ui.settings.SettingsScreen
import dev.chrisotm.barbelltracker.ui.stats.StatisticsScreen
import dev.chrisotm.barbelltracker.ui.workout.ActiveWorkoutScreen
import dev.chrisotm.barbelltracker.ui.workout.WorkoutOverviewScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    AppScaffold(nav) { mod ->
        NavHost(navController = nav, startDestination = Routes.PLANS, modifier = mod) {

        composable(Routes.PLANS) {
            PlansListScreen(
                onOpenPlan = { nav.navigate(Routes.planEdit(it)) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onOpenExercise = { nav.navigate(Routes.exerciseProgress(it)) }
            )
        }

        composable(Routes.EXERCISES) {
            ExerciseLibraryScreen(
                onOpen = { nav.navigate(Routes.exerciseEdit(it)) }
            )
        }

        composable(
            route = "${Routes.EXERCISE_EDIT}?exerciseId={exerciseId}",
            arguments = listOf(navArgument("exerciseId") {
                type = NavType.LongType; defaultValue = 0L
            })
        ) {
            ExerciseEditScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.PLAN_CREATE) {
            PlanCreateScreen(
                onBack = { nav.popBackStack() },
                onCreated = { planId ->
                    nav.navigate(Routes.planEdit(planId)) {
                        popUpTo(Routes.PLAN_CREATE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Routes.PLAN_EDIT}/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) {
            PlanEditScreen(
                onBack = { nav.popBackStack() },
                onStartWorkout = { nav.navigate(Routes.workoutOverview(it)) }
            )
        }

        composable(
            route = "${Routes.WORKOUT_OVERVIEW}/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutOverviewScreen(
                onBack = { nav.popBackStack() },
                onStart = { workoutId ->
                    nav.navigate(Routes.activeWorkout(workoutId)) {
                        popUpTo("${Routes.WORKOUT_OVERVIEW}/{workoutId}") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Routes.ACTIVE_WORKOUT}/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            ActiveWorkoutScreen(
                onExit = {
                    nav.navigate(Routes.PLANS) {
                        popUpTo(Routes.PLANS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryListScreen(
                onOpenSession = { nav.navigate(Routes.sessionDetail(it)) }
            )
        }

        composable(
            route = "${Routes.SESSION_DETAIL}/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            SessionDetailScreen(
                onBack = { nav.popBackStack() },
                onOpenProgress = { nav.navigate(Routes.exerciseProgress(it)) }
            )
        }

        composable(
            route = "${Routes.EXERCISE_PROGRESS}/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            ExerciseProgressScreen(onBack = { nav.popBackStack() })
        }
        }
    }
}
