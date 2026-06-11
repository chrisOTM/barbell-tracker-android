package dev.chrisotm.barbelltracker.data.repo

import dev.chrisotm.barbelltracker.data.dao.SessionDao
import dev.chrisotm.barbelltracker.data.entity.SessionSet
import dev.chrisotm.barbelltracker.data.entity.SessionWithSets
import dev.chrisotm.barbelltracker.data.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SessionRepository {
    suspend fun startSession(planId: Long, workoutId: Long, planName: String, workoutName: String): Long
    suspend fun logSet(set: SessionSet): Long
    suspend fun finishSession(session: WorkoutSession)
    fun observeSessions(): Flow<List<WorkoutSession>>
    fun observeSessionsWithSets(): Flow<List<SessionWithSets>>
    fun observeSession(id: Long): Flow<SessionWithSets?>
    fun observeSetsForExercise(exerciseId: Long): Flow<List<SessionSet>>
    suspend fun lastWeightFor(exerciseId: Long): Double?
}

class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {
    override suspend fun startSession(
        planId: Long,
        workoutId: Long,
        planName: String,
        workoutName: String
    ): Long = dao.insertSession(
        WorkoutSession(
            planId = planId,
            workoutId = workoutId,
            planName = planName,
            workoutName = workoutName,
            startedAt = System.currentTimeMillis()
        )
    )

    override suspend fun logSet(set: SessionSet): Long = dao.insertSet(set)

    override suspend fun finishSession(session: WorkoutSession) = dao.updateSession(session)

    override fun observeSessions(): Flow<List<WorkoutSession>> = dao.observeSessions()

    override fun observeSessionsWithSets(): Flow<List<SessionWithSets>> = dao.observeSessionsWithSets()

    override fun observeSession(id: Long): Flow<SessionWithSets?> = dao.observeSessionWithSets(id)

    override fun observeSetsForExercise(exerciseId: Long): Flow<List<SessionSet>> =
        dao.observeSetsForExercise(exerciseId)

    override suspend fun lastWeightFor(exerciseId: Long): Double? = dao.lastWeightFor(exerciseId)
}
