package com.czyz.fittracker.dao

import androidx.room.Dao
import androidx.room.*
import com.czyz.fittracker.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // --- CUD (Create, Update, Delete) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)


    // --- ZAPYTANIA (READ) ---

    // Pobiera wszystkie ćwiczenia z katalogu (w kolejności dodania przez użytkownika)
    @Query("SELECT * FROM exercises ORDER BY exerciseId ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    // Pobiera ćwiczenia przypisane do konkretnego planu
    @Query("SELECT * FROM exercises WHERE planId = :planId ORDER BY exerciseId ASC")
    fun getExercisesForPlan(planId: Int): Flow<List<ExerciseEntity>>

    // Pobiera (suspend) ćwiczenia przypisane do konkretnego planu
    @Query("SELECT * FROM exercises WHERE planId = :planId ORDER BY exerciseId ASC")
    suspend fun getExercisesForPlanOnce(planId: Int): List<ExerciseEntity>

    // Usuwa wszystkie ćwiczenia przypisane do danego planu (pomocnicze przy aktualizacji)
    @Query("DELETE FROM exercises WHERE planId = :planId AND exerciseId NOT IN (SELECT exerciseId FROM workout_exercises)")
    suspend fun deleteUnusedExercisesForPlan(planId: Int)

    // Wyszukiwanie ćwiczeń po frazie (dla wyszukiwarki w UI)
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY exerciseId ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    // Pobiera jedno konkretne ćwiczenie po ID
    @Query("SELECT * FROM exercises WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getExerciseById(exerciseId: Int): ExerciseEntity?
}