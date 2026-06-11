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
import dev.chrisotm.barbelltracker.ui.workout.ActiveWorkoutScreen
import dev.chrisotm.barbelltracker.ui.workout.WorkoutOverviewScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.PLANS) {

        composable(Routes.PLANS) {
            PlansListScreen(
                onOpenPlan = { nav.navigate(Routes.planEdit(it)) },
                onCreatePlan = { nav.navigate(Routes.PLAN_CREATE) },
                onOpenExercises = { nav.navigate(Routes.EXERCISES) },
                onOpenHistory = { nav.navigate(Routes.HISTORY) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.EXERCISES) {
            ExerciseLibraryScreen(
                onBack = { nav.popBackStack() },
                onAdd = { nav.navigate(Routes.exerciseEdit(0)) },
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
                onBack = { nav.popBackStack() },
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
