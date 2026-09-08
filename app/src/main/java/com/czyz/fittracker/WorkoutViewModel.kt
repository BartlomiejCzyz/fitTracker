package com.czyz.fittracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.czyz.fittracker.database.AppDatabase
import com.czyz.fittracker.entity.*
import com.czyz.fittracker.repository.ExerciseRepository
import com.czyz.fittracker.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    // --- STRUMIENIE DANYCH Z BAZY ---
    val plans: StateFlow<List<WorkoutPlanEntity>> = workoutRepository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlanId = MutableStateFlow<Int?>(null)
    val selectedPlanId: StateFlow<Int?> = _selectedPlanId.asStateFlow()

    // Wszystkie treningi wraz ze szczegółami
    val workoutsWithDetails: StateFlow<List<WorkoutWithDetails>> = workoutRepository.allWorkoutsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ćwiczenia dla aktualnie wybranego planu (reaktywny strumień)
    @OptIn(ExperimentalCoroutinesApi::class)
    val planExercises: StateFlow<List<ExerciseEntity>> = _selectedPlanId
        .flatMapLatest { planId ->
            if (planId != null) {
                exerciseRepository.getExercisesForPlan(planId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPlan(planId: Int?) {
        _selectedPlanId.value = planId
    }

    // Zwraca reaktywny Flow ćwiczeń dla dowolnego planu (do użytku w UI)
    fun getExercisesFlowForPlan(planId: Int): Flow<List<ExerciseEntity>> {
        return exerciseRepository.getExercisesForPlan(planId)
    }

    // --- OPERACJE NA PLANACH I SLOWNIKU ---

    fun savePlan(
        planId: Int?,
        title: String,
        description: String,
        exercises: List<Pair<String, Int>> // Nazwa, TargetReps
    ) {
        viewModelScope.launch {
            val isNew = planId == null || planId == 0
            val targetPlanId: Int

            if (isNew) {
                // Nowy plan — wstaw plan i uzyskaj jego ID
                targetPlanId = workoutRepository.insertPlan(
                    WorkoutPlanEntity(title = title, description = description)
                ).toInt()
            } else {
                // Aktualizacja istniejącego planu
                workoutRepository.updatePlan(
                    WorkoutPlanEntity(workoutPlanId = planId!!, title = title, description = description)
                )
                targetPlanId = planId
            }

            // Pobierz dotychczasowe ćwiczenia TYLKO tego planu
            val existingExercises = exerciseRepository.getExercisesForPlanOnce(targetPlanId)
            val existingByName = existingExercises.associateBy { it.name.trim().lowercase() }
            val newNamesSet = exercises.map { it.first.trim().lowercase() }.toSet()

            // 1. Zaktualizuj istniejące lub dodaj nowe ćwiczenia (z planId!)
            exercises.forEach { (name, targetReps) ->
                val trimmedName = name.trim()
                if (trimmedName.isNotBlank()) {
                    val existing = existingByName[trimmedName.lowercase()]
                    if (existing != null) {
                        exerciseRepository.updateExercise(
                            existing.copy(name = trimmedName, targetRepetitions = targetReps)
                        )
                    } else {
                        exerciseRepository.insertExercise(
                            ExerciseEntity(
                                name = trimmedName,
                                targetRepetitions = targetReps,
                                targetWeight = 0.0,
                                planId = targetPlanId
                            )
                        )
                    }
                }
            }

            // 2. Bezpiecznie usuń ćwiczenia tego planu, których nie ma już w formularzu
            //    i które NIE są użyte w żadnej sesji (ForeignKey RESTRICT nie pozwoli)
            existingExercises.forEach { existing ->
                if (existing.name.trim().lowercase() !in newNamesSet) {
                    // deleteUnusedExercisesForPlan usuwa tylko te bez sesji — bezpieczne
                    try {
                        exerciseRepository.deleteExercise(existing)
                    } catch (e: Exception) {
                        // Ćwiczenie jest użyte w sesji — zostaw je (nie kasuj danych treningowych)
                    }
                }
            }
        }
    }

    fun deletePlan(plan: WorkoutPlanEntity) {
        viewModelScope.launch {
            workoutRepository.deletePlan(plan)
            if (_selectedPlanId.value == plan.workoutPlanId) {
                _selectedPlanId.value = null
            }
        }
    }

    // --- OPERACJE NA SESJACH TRENINGOWYCH I SERIACH ---

    fun addWorkoutDay(planId: Int, dateTitle: String) {
        viewModelScope.launch {
            val workoutId = workoutRepository.insertWorkout(
                WorkoutEntity(
                    planId = planId,
                    title = dateTitle,
                    dateTimestamp = System.currentTimeMillis()
                )
            ).toInt()

            // Pobierz TYLKO ćwiczenia tego konkretnego planu
            val planExerciseList = exerciseRepository.getExercisesForPlanOnce(planId)
            planExerciseList.forEach { catalogEx ->
                val workoutExerciseId = workoutRepository.addExerciseToWorkout(workoutId, catalogEx.exerciseId).toInt()

                // Domyślnie utwórz 3 puste serie
                for (setNum in 1..3) {
                    workoutRepository.addSet(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setNum,
                        weight = 0.0,
                        reps = 0
                    )
                }
            }
        }
    }

    fun deleteWorkoutDay(workoutId: Int) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutById(workoutId)
        }
    }

    fun updateSetData(setId: Int, workoutExerciseId: Int, setNumber: Int, weight: Double, reps: Int) {
        viewModelScope.launch {
            if (setId == 0) {
                workoutRepository.addSet(workoutExerciseId, setNumber, weight, reps)
            } else {
                workoutRepository.updateSet(
                    ExerciseSetEntity(
                        setId = setId,
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setNumber,
                        weightKg = weight,
                        reps = reps
                    )
                )
            }
        }
    }
}

// Fabryka instancji ViewModelu dla Jetpack Compose
class WorkoutViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            val exerciseRepo = ExerciseRepository(database.exerciseDao())
            val workoutRepo = WorkoutRepository(database.workoutDao())
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(workoutRepo, exerciseRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}