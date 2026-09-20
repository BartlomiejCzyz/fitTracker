package com.czyz.fittracker.dao

import androidx.room.*
import com.czyz.fittracker.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ==========================================
    // 1. PLAN TRENINGOWY (WorkoutPlanEntity)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: WorkoutPlanEntity)

    @Delete
    suspend fun deletePlan(plan: WorkoutPlanEntity)

    @Query("SELECT * FROM workout_plans ORDER BY workoutPlanId DESC")
    fun getAllPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE workoutPlanId = :planId")
    fun getPlanById(planId: Int): Flow<WorkoutPlanEntity?>


    // ==========================================
    // 2. SESJE TRENINGOWE (WorkoutEntity)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE workoutId = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Int)

    // Wszystkie treningi w ramach jednego planu
    @Query("SELECT * FROM workouts WHERE planId = :planId ORDER BY dateTimestamp ASC")
    fun getWorkoutsForPlan(planId: Int): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE planId = :planId ORDER BY dateTimestamp ASC")
    suspend fun getWorkoutsForPlanOnce(planId: Int): List<WorkoutEntity>


    // ==========================================
    // 3. ŁĄCZNIK TRENING-ĆWICZENIE (WorkoutExerciseEntity)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(workoutExercises: List<WorkoutExerciseEntity>): List<Long>

    @Delete
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExerciseEntity)


    // ==========================================
    // 4. SERIE (ExerciseSetEntity)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Update
    suspend fun updateSet(set: ExerciseSetEntity)

    @Delete
    suspend fun deleteSet(set: ExerciseSetEntity)

    @Query("DELETE FROM exercise_sets WHERE setId = :setId")
    suspend fun deleteSetById(setId: Int)


    // ==========================================
    // 5. COMPLEX RELATIONAL QUERIES (ODCZYT CAŁYCH DRZEW DANYCH)
    // ==========================================

    // Pobiera kompletny trening (z ćwiczeniami ze słownika oraz seriami)
    @Transaction
    @Query("SELECT * FROM workouts WHERE workoutId = :workoutId")
    fun getWorkoutWithDetails(workoutId: Int): Flow<WorkoutWithDetails?>

    // Pobiera historię wszystkich treningów z ich pełnymi szczegółami
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY dateTimestamp ASC")
    fun getAllWorkoutsWithDetails(): Flow<List<WorkoutWithDetails>>
}