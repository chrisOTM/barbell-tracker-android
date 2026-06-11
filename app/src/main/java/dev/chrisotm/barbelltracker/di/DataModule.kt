package dev.chrisotm.barbelltracker.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.chrisotm.barbelltracker.data.dao.ExerciseDao
import dev.chrisotm.barbelltracker.data.dao.PlanDao
import dev.chrisotm.barbelltracker.data.dao.SessionDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutExerciseDao
import dev.chrisotm.barbelltracker.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        exerciseDaoProvider: Provider<ExerciseDao>
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "barbell_tracker.db"
    )
        .addCallback(AppDatabase.SeedCallback(context, scope, exerciseDaoProvider))
        .build()

    @Provides fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun providePlanDao(db: AppDatabase): PlanDao = db.planDao()
    @Provides fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideWorkoutExerciseDao(db: AppDatabase): WorkoutExerciseDao = db.workoutExerciseDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
}
