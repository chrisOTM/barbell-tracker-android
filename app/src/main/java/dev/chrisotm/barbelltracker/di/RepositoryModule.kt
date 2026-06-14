package dev.chrisotm.barbelltracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.chrisotm.barbelltracker.data.repo.BackupRepository
import dev.chrisotm.barbelltracker.data.repo.BackupRepositoryImpl
import dev.chrisotm.barbelltracker.data.repo.ExerciseRepository
import dev.chrisotm.barbelltracker.data.repo.ExerciseRepositoryImpl
import dev.chrisotm.barbelltracker.data.repo.PlanRepository
import dev.chrisotm.barbelltracker.data.repo.PlanRepositoryImpl
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.data.repo.SessionRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds @Singleton
    abstract fun bindPlanRepository(impl: PlanRepositoryImpl): PlanRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
