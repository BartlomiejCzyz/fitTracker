package com.czyz.fittracker.repository


import com.czyz.fittracker.dao.WorkoutDao
import com.czyz.fittracker.entity.*
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    // --- PLAN TRENINGOWY ---

    val allPlans: Flow<List<WorkoutPlanEntity>> = workoutDao.getAllPlans()

    fun getPlanById(planId: Int): Flow<WorkoutPlanEntity?> {
        return workoutDao.getPlanById(planId)
    }

    suspend fun insertPlan(plan: WorkoutPlanEntity): Long {
        return workoutDao.insertPlan(plan)
    }

    suspend fun updatePlan(plan: WorkoutPlanEntity) {
        workoutDao.updatePlan(plan)
    }

    suspend fun deletePlan(plan: WorkoutPlanEntity) {
        workoutDao.deletePlan(plan)
    }


    // --- SESJE TRENINGOWE ---

    val allWorkoutsWithDetails: Flow<List<WorkoutWithDetails>> =
        workoutDao.getAllWorkoutsWithDetails()

    fun getWorkoutsForPlan(planId: Int): Flow<List<WorkoutEntity>> {
        return workoutDao.getWorkoutsForPlan(planId)
    }

    fun getWorkoutWithDetails(workoutId: Int): Flow<WorkoutWithDetails?> {
        return workoutDao.getWorkoutWithDetails(workoutId)
    }

    suspend fun insertWorkout(workout: WorkoutEntity): Long {
        return workoutDao.insertWorkout(workout)
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        workoutDao.deleteWorkout(workout)
    }

    suspend fun deleteWorkoutById(workoutId: Int) {
        workoutDao.deleteWorkoutById(workoutId)
    }


    // --- ŁĄCZENIE ĆWICZEŃ Z TRENINGIEM ORAZ SERIE ---

    // Dodanie ćwiczenia ze słownika do treningu
    suspend fun addExerciseToWorkout(workoutId: Int, exerciseId: Int): Long {
        val workoutExercise = WorkoutExerciseEntity(
            workoutId = workoutId,
            exerciseId = exerciseId
        )
        return workoutDao.insertWorkoutExercise(workoutExercise)
    }

    suspend fun removeExerciseFromWorkout(workoutExercise: WorkoutExerciseEntity) {
        workoutDao.deleteWorkoutExercise(workoutExercise)
    }

    // Zarządzanie seriami
    suspend fun addSet(workoutExerciseId: Int, setNumber: Int, weight: Double, reps: Int): Long {
        val set = ExerciseSetEntity(
            workoutExerciseId = workoutExerciseId,
            setNumber = setNumber,
            weightKg = weight,
            reps = reps
        )
        return workoutDao.insertSet(set)
    }

    suspend fun updateSet(set: ExerciseSetEntity) {
        workoutDao.updateSet(set)
    }

    suspend fun deleteSet(set: ExerciseSetEntity) {
        workoutDao.deleteSet(set)
    }

    suspend fun deleteSetById(setId: Int) {
        workoutDao.deleteSetById(setId)
    }
}