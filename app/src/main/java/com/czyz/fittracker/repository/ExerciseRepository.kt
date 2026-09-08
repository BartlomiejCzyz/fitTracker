package com.czyz.fittracker.repository

import com.czyz.fittracker.entity.ExerciseEntity
import com.czyz.fittracker.dao.ExerciseDao
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {

    // Strumień wszystkich ćwiczeń ze słownika
    val allExercises: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    // Strumień ćwiczeń przypisanych do konkretnego planu
    fun getExercisesForPlan(planId: Int): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesForPlan(planId)
    }

    // Jednorazowe pobranie ćwiczeń danego planu (suspend)
    suspend fun getExercisesForPlanOnce(planId: Int): List<ExerciseEntity> {
        return exerciseDao.getExercisesForPlanOnce(planId)
    }

    // Usuń nieużywane ćwiczenia danego planu (bez powiązanych sesji)
    suspend fun deleteUnusedExercisesForPlan(planId: Int) {
        exerciseDao.deleteUnusedExercisesForPlan(planId)
    }

    // Wyszukiwanie ćwiczeń
    fun searchExercises(query: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.searchExercises(query)
    }

    // Pobranie pojedynczego ćwiczenia po ID
    suspend fun getExerciseById(id: Int): ExerciseEntity? {
        return exerciseDao.getExerciseById(id)
    }

    // Operacje zapisu i edycji
    suspend fun insertExercise(exercise: ExerciseEntity): Long {
        return exerciseDao.insertExercise(exercise)
    }

    suspend fun updateExercise(exercise: ExerciseEntity) {
        exerciseDao.updateExercise(exercise)
    }

    suspend fun deleteExercise(exercise: ExerciseEntity) {
        exerciseDao.deleteExercise(exercise)
    }
}