package dev.chrisotm.barbelltracker.data.repo

import dev.chrisotm.barbelltracker.data.dao.ExerciseDao
import dev.chrisotm.barbelltracker.data.entity.Exercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Library access. Interface keeps the rest of the app independent of Room so a
 *  remote source can be swapped in later (US-5.2). */
interface ExerciseRepository {
    fun observeAll(): Flow<List<Exercise>>
    suspend fun getById(id: Long): Exercise?
    suspend fun upsert(exercise: Exercise): Long
    suspend fun delete(exercise: Exercise)
}

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao
) : ExerciseRepository {
    override fun observeAll(): Flow<List<Exercise>> = dao.observeAll()
    override suspend fun getById(id: Long): Exercise? = dao.getById(id)
    override suspend fun upsert(exercise: Exercise): Long =
        if (exercise.id == 0L) dao.insert(exercise)
        else { dao.update(exercise); exercise.id }
    override suspend fun delete(exercise: Exercise) = dao.delete(exercise)
}
