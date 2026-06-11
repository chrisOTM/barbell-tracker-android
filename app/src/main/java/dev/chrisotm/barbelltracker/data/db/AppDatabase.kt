package dev.chrisotm.barbelltracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.chrisotm.barbelltracker.data.dao.ExerciseDao
import dev.chrisotm.barbelltracker.data.dao.PlanDao
import dev.chrisotm.barbelltracker.data.dao.SessionDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutExerciseDao
import dev.chrisotm.barbelltracker.data.entity.Exercise
import dev.chrisotm.barbelltracker.data.entity.Plan
import dev.chrisotm.barbelltracker.data.entity.SessionSet
import dev.chrisotm.barbelltracker.data.entity.Workout
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(
    entities = [
        Exercise::class,
        Plan::class,
        Workout::class,
        WorkoutExercise::class,
        WorkoutSession::class,
        SessionSet::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun planDao(): PlanDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun sessionDao(): SessionDao

    /** Seeds the predefined exercise library on first creation, in the active language (US-1.1). */
    class SeedCallback(
        private val context: android.content.Context,
        private val scope: CoroutineScope,
        private val exerciseDaoProvider: Provider<ExerciseDao>
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch {
                exerciseDaoProvider.get().insertAll(SeedData.exercises(context))
            }
        }
    }
}
