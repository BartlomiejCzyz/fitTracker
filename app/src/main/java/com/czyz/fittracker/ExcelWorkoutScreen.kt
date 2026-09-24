package com.czyz.fittracker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.czyz.fittracker.entity.ExerciseEntity
import com.czyz.fittracker.entity.ExerciseSetEntity
import com.czyz.fittracker.entity.WorkoutPlanEntity
import com.czyz.fittracker.entity.WorkoutWithDetails
import com.czyz.fittracker.timer.RestTimerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- PALETA KOLORÓW DARK MODE ---
val DarkBackground = Color(0xFF0A0A0A)
val DarkSurface = Color(0xFF141414)
val DarkSurfaceHeader = Color(0xFF1E1E1E)
val AccentGreen = Color(0xFF4CAF50)
val NeonPurple = Color(0xFF7245FA)
val DangerRed = Color(0xFFE53935)
val BorderDark = Color(0xFF2A2A2A)
val TextWhite = Color(0xFFE0E0E0)
val TextMuted = Color(0xFF777777)

// --- MODELE DANYCH UI ---
data class ExerciseUi(
    val id: Int = 0,
    var name: String,
    var targetReps: Int,
    var targetSets: Int = 3
)

// --- ZARZĄDCA EKRANÓW ---
@Composable
fun MainAppNavigation(viewModel: WorkoutViewModel) {
    val plans by viewModel.plans.collectAsState()
    val selectedPlanId by viewModel.selectedPlanId.collectAsState()
    val planExercises by viewModel.planExercises.collectAsState()

    val selectedPlanEntity = plans.find { it.workoutPlanId == selectedPlanId }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        if (selectedPlanEntity == null) {
            PlanListScreen(
                plans = plans,
                workoutsWithDetails = viewModel.workoutsWithDetails.collectAsState().value,
                onPlanClick = { planEntity ->
                    viewModel.selectPlan(planEntity.workoutPlanId)
                },
                onSavePlan = { planToEdit, name, description, exercises ->
                    viewModel.savePlan(
                        planId = planToEdit?.workoutPlanId,
                        title = name,
                        description = description,
                        exercises = exercises
                    )
                },
                onDeletePlan = { planEntity ->
                    viewModel.deletePlan(planEntity)
                },
                getPlanExercises = { planId ->
                    viewModel.getExercisesFlowForPlan(planId)
                }
            )
        } else {
            ExcelWorkoutScreen(
                viewModel = viewModel,
                plan = selectedPlanEntity,
                planExercises = planExercises,
                onBackClick = { viewModel.selectPlan(null) }
            )
        }
    }
}

// ===================================
// EKRAN 1: LISTA PLANÓW TRENINGOWYCH
// ===================================
@Composable
fun PlanListScreen(
    plans: List<WorkoutPlanEntity>,
    workoutsWithDetails: List<WorkoutWithDetails>,
    onPlanClick: (WorkoutPlanEntity) -> Unit,
    onSavePlan: (planToEdit: WorkoutPlanEntity?, name: String, description: String, exercises: List<ExerciseUi>) -> Unit,
    onDeletePlan: (WorkoutPlanEntity) -> Unit,
    getPlanExercises: (planId: Int) -> Flow<List<ExerciseEntity>>
) {
    var showDialog by remember { mutableStateOf(false) }
    var planToEdit by remember { mutableStateOf<WorkoutPlanEntity?>(null) }

    // Reaktywnie ładujemy ćwiczenia dla aktualnie edytowanego planu
    val editingPlanExercises by remember(planToEdit) {
        if (planToEdit != null) {
            getPlanExercises(planToEdit!!.workoutPlanId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Moje Plany",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
                Text(
                    text = "Wybierz plan, aby rejestrować postępy",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(DarkSurfaceHeader, RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderDark, RoundedCornerShape(8.dp))
                    .clickable {
                        planToEdit = null
                        showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
            }
        }

        if (plans.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Brak zapisanych planów.\nKliknij '+', aby dodać nowy plan.",
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plans) { plan ->
                    // Liczba ćwiczeń dla tego konkretnego planu
                    val planExercisesCount = workoutsWithDetails
                        .filter { it.workout.planId == plan.workoutPlanId }
                        .firstOrNull()?.exercises?.size
                        ?: 0
                    PlanCard(
                        plan = plan,
                        exerciseCount = planExercisesCount,
                        onClick = { onPlanClick(plan) },
                        onEditClick = {
                            planToEdit = plan
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        PlanDialog(
            planToEdit = planToEdit,
            initialExercises = editingPlanExercises,
            onDismiss = { showDialog = false },
            onConfirm = { name, description, exercises ->
                onSavePlan(planToEdit, name, description, exercises)
                showDialog = false
            },
            onDelete = {
                if (planToEdit != null) {
                    onDeletePlan(planToEdit!!)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun PlanCard(
    plan: WorkoutPlanEntity,
    exerciseCount: Int,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plan.description,
                    fontSize = 13.sp,
                    color = TextWhite
                )
                /*
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Liczba ćwiczeń: $exerciseCount",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                */

            }

            Text(
                text = "Edytuj",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonPurple,
                modifier = Modifier
                    .clickable { onEditClick() }
                    .padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

// =========================================
// OKNO DIALOGOWE: EDYCJA PLANU I ĆWICZEŃ
// =========================================
@Composable
fun PlanDialog(
    planToEdit: WorkoutPlanEntity?,
    initialExercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, exercises: List<ExerciseUi>) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(planToEdit?.title ?: "") }
    var description by remember { mutableStateOf(planToEdit?.description ?: "") }

    // Inicjujemy listę ćwiczeń na podstawie ćwiczeń tego planu (pusta dla nowego planu)
    val exercises = remember { mutableStateListOf<ExerciseUi>() }
    // Jednorazowo załaduj ćwiczenia z bazy gdy się pojawią (np. po otwarciu dialogu edycji)
    var exercisesLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(initialExercises) {
        if (!exercisesLoaded && initialExercises.isNotEmpty()) {
            exercises.clear()
            exercises.addAll(initialExercises.map { ExerciseUi(it.exerciseId, it.name, it.targetRepetitions, it.targetSets) })
            exercisesLoaded = true
        } else if (!exercisesLoaded && planToEdit == null) {
            // Nowy plan — nie ładujemy niczego, lista zaczyna pusta
            exercisesLoaded = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceHeader,
        title = {
            Text(
                text = if (planToEdit == null) "Nowy Plan" else "Edytuj Plan",
                color = AccentGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa planu", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen, unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedLabelColor = AccentGreen
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen, unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedLabelColor = AccentGreen
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = BorderDark)

                Text("Ćwiczenia w planie:", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                if (exercises.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = "Nazwa ćwiczenia",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Serie",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(52.dp)
                        )
                        Text(
                            text = "Powt.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPurple,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(52.dp)
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                }

                exercises.forEachIndexed { index, exercise ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Przyciski do zmiany kolejności ćwiczeń
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "▲",
                                color = if (index > 0) NeonPurple else TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable(enabled = index > 0) {
                                        val tmp = exercises[index - 1]
                                        exercises[index - 1] = exercises[index]
                                        exercises[index] = tmp
                                    }
                                    .padding(2.dp)
                            )
                            Text(
                                text = "▼",
                                color = if (index < exercises.lastIndex) NeonPurple else TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable(enabled = index < exercises.lastIndex) {
                                        val tmp = exercises[index + 1]
                                        exercises[index + 1] = exercises[index]
                                        exercises[index] = tmp
                                    }
                                    .padding(2.dp)
                            )
                        }

                        OutlinedTextField(
                            value = exercise.name,
                            onValueChange = { newName ->
                                exercises[index] = exercise.copy(name = newName)
                            },
                            placeholder = { Text("Nazwa", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen, unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                            )
                        )

                        OutlinedTextField(
                            value = if (exercise.targetSets > 0) exercise.targetSets.toString() else "",
                            onValueChange = { sets ->
                                val parsed = sets.toIntOrNull() ?: 0
                                exercises[index] = exercise.copy(targetSets = parsed)
                            },
                            placeholder = { Text("Serie", fontSize = 10.sp, color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(52.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen, unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                            )
                        )

                        OutlinedTextField(
                            value = if (exercise.targetReps > 0) exercise.targetReps.toString() else "",
                            onValueChange = { reps ->
                                val parsed = reps.toIntOrNull() ?: 0
                                exercises[index] = exercise.copy(targetReps = parsed)
                            },
                            placeholder = { Text("Powt.", fontSize = 10.sp, color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(52.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen, unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                            )
                        )

                        Text(
                            text = "✕",
                            color = DangerRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { exercises.removeAt(index) }
                                .padding(4.dp)
                        )
                    }
                }

                TextButton(
                    onClick = {
                        exercises.add(ExerciseUi(0, "", 10, 3))
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("+ Dodaj ćwiczenie", color = AccentGreen, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description, exercises)
                    }
                }
            ) {
                Text("Zapisz", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (planToEdit != null) {
                    TextButton(onClick = onDelete) {
                        Text("Usuń plan", color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Anuluj", color = TextMuted)
                }
            }
        }
    )
}

// ======================
// EKRAN 2: WIDOK EXCELA
// ======================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExcelWorkoutScreen(
    viewModel: WorkoutViewModel,
    plan: WorkoutPlanEntity,
    planExercises: List<ExerciseEntity>,
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }
    val workoutsWithDetails by viewModel.workoutsWithDetails.collectAsState()

    val planWorkouts = remember(workoutsWithDetails, plan.workoutPlanId) {
        workoutsWithDetails.filter { it.workout.planId == plan.workoutPlanId }
    }

    var expandedDayId by remember { mutableStateOf<Int?>(null) }
    var exerciseToEdit by remember { mutableStateOf<ExerciseEntity?>(null) }

    val horizontalScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current
    val collapsedWidthPx = with(density) { 70.dp.toPx() }.toInt()

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // TOP BAR (z przyciskiem usuwania dnia w prawym górnym rogu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(DarkSurfaceHeader)
                    .border(0.5.dp, BorderDark)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "← Plany",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .padding(end = 16.dp)
                    )
                    Text(
                        text = plan.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        maxLines = 1
                    )
                }

                if (expandedDayId != null) {
                    val expandedWorkoutDetails = planWorkouts.find { it.workout.workoutId == expandedDayId }
                    if (expandedWorkoutDetails != null) {
                        TextButton(
                            onClick = {
                                val wId = expandedDayId!!
                                expandedDayId = null
                                viewModel.deleteWorkoutDay(wId)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🗑 Usuń dzień",
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            if (planExercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Brak ćwiczeń w bazie.\nWróć i kliknij 'Edytuj', aby dodać ćwiczenia do planu.",
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // NAGŁÓWEK TABELI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceHeader)
                ) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(60.dp)
                            .border(0.5.dp, BorderDark)
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("Ćwiczenie", fontWeight = FontWeight.Bold, color = AccentGreen)
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                    ) {
                        planWorkouts.forEachIndexed { index, workoutWithDetails ->
                            val workout = workoutWithDetails.workout
                            val isExpanded = (expandedDayId == workout.workoutId)

                            val sameTitleWorkouts = planWorkouts.filter { it.workout.title == workout.title }
                            val titleIndex = sameTitleWorkouts.indexOf(workoutWithDetails)
                            val displayTitle = if (sameTitleWorkouts.size > 1) {
                                "${workout.title} (#${titleIndex + 1})"
                            } else {
                                workout.title
                            }

                            DayHeaderCell(
                                dateTitle = displayTitle,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    if (isExpanded) {
                                        expandedDayId = null
                                    } else {
                                        expandedDayId = workout.workoutId
                                        coroutineScope.launch {
                                            delay(50)
                                            val targetScroll = index * collapsedWidthPx
                                            horizontalScrollState.animateScrollTo(targetScroll)
                                        }
                                    }
                                }
                            )
                        }


                        // Dodanie Nowego Dnia
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(60.dp)
                                .border(0.5.dp, BorderDark)
                                .clickable {
                                    val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
                                    val currentDate = sdf.format(Date())
                                    viewModel.addWorkoutDay(plan.workoutPlanId, currentDate)

                                    coroutineScope.launch {
                                        delay(150)
                                        val targetScroll = (planWorkouts.size) * collapsedWidthPx
                                        horizontalScrollState.animateScrollTo(targetScroll)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                        }
                    }
                }

                // WIERSZE Z ĆWICZENIAMI
                LazyColumn(
                    modifier = Modifier.imePadding(),
                    contentPadding = PaddingValues(bottom = 200.dp)
                ) {
                    items(planExercises) { exercise ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(90.dp)
                                    .border(0.5.dp, BorderDark)
                                    .background(DarkSurface)
                                    .clickable { exerciseToEdit = exercise }
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = exercise.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = TextWhite,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = "${exercise.targetSets} serie • ${exercise.targetRepetitions} powt.",
                                            fontSize = 11.sp,
                                            color = NeonPurple
                                        )
                                    }
                                    Text(
                                        text = "✎ Edytuj",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(horizontalScrollState)
                            ) {
                                planWorkouts.forEach { workoutWithDetails ->
                                    val isExpanded = (expandedDayId == workoutWithDetails.workout.workoutId)
                                    val workoutExerciseDetails = workoutWithDetails.exercises.find {
                                        it.exerciseCatalog.exerciseId == exercise.exerciseId
                                    }
                                    val workoutExerciseId = workoutExerciseDetails?.workoutExercise?.workoutExerciseId ?: 0
                                    val sets = workoutExerciseDetails?.sets ?: emptyList()

                                    DayDataCell(
                                        isExpanded = isExpanded,
                                        sets = sets,
                                        onToggleExpand = {
                                            if (isExpanded) {
                                                expandedDayId = null
                                            } else {
                                                expandedDayId = workoutWithDetails.workout.workoutId
                                            }
                                        },
                                        onSetUpdate = { setIndex, newWeight, newReps ->
                                            val existingSet = sets.find { it.setNumber == setIndex + 1 }
                                            val setId = existingSet?.setId ?: 0
                                            viewModel.updateSetData(
                                                workoutId = workoutWithDetails.workout.workoutId,
                                                exerciseId = exercise.exerciseId,
                                                setId = setId,
                                                workoutExerciseId = workoutExerciseId,
                                                setNumber = setIndex + 1,
                                                weight = newWeight,
                                                reps = newReps
                                            )
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(90.dp)
                                        .border(0.5.dp, BorderDark)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (exerciseToEdit != null) {
            EditExerciseDialog(
                exercise = exerciseToEdit!!,
                onDismiss = { exerciseToEdit = null },
                onSave = { newName, newReps, newSets ->
                    viewModel.updateExercise(exerciseToEdit!!.exerciseId, newName, newReps, newSets)
                    exerciseToEdit = null
                },
                onDelete = {
                    viewModel.deleteExercise(exerciseToEdit!!)
                    exerciseToEdit = null
                }
            )
        }
    }
}

// --- KOMPONENTY POMOCNICZE TABELI ---

@Composable
fun DayHeaderCell(
    dateTitle: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val width = if (isExpanded) 180.dp else 70.dp
    val bg = if (isExpanded) Color(0xFF16251C) else DarkSurfaceHeader

    Box(
        modifier = Modifier
            .width(width)
            .height(60.dp)
            .border(0.5.dp, BorderDark)
            .background(bg)
            .clickable { onToggleExpand() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = dateTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentGreen)
            Text(
                text = if (isExpanded) "▲ Zwiń" else "▼ Rozwiń",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}


@Composable
fun DayDataCell(
    isExpanded: Boolean,
    sets: List<ExerciseSetEntity>,
    onToggleExpand: () -> Unit,
    onSetUpdate: (setIndex: Int, weight: Double, reps: Int) -> Unit
) {
    val context = LocalContext.current
    if (!isExpanded) {
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(90.dp)
                .border(0.5.dp, BorderDark)
                .background(DarkBackground)
                .clickable { onToggleExpand() },
            contentAlignment = Alignment.Center
        ) {
            val lastSet = sets.lastOrNull() { it.weightKg > 0 || it.reps > 0 } ?: sets.lastOrNull()
            if (lastSet != null && (lastSet.weightKg > 0 || lastSet.reps > 0)) {
                val weightStr = if (lastSet.weightKg % 1.0 == 0.0) lastSet.weightKg.toInt().toString() else lastSet.weightKg.toString()
                Text(
                    text = "${weightStr}kg\nx${lastSet.reps}",
                    fontSize = 11.sp,
                    color = AccentGreen,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(text = "—", color = TextMuted)
            }
        }
    } else {
        Row(
            modifier = Modifier
                .width(180.dp)
                .height(90.dp)
                .border(0.5.dp, BorderDark)
                .background(DarkBackground)
        ) {
            for (setIndex in 0..2) {
                val currentSet = sets.find { it.setNumber == setIndex + 1 }
                val weightVal = currentSet?.weightKg ?: 0.0
                val repsVal = currentSet?.reps ?: 0

                val weightStr = if (weightVal > 0) {
                    if (weightVal % 1.0 == 0.0) weightVal.toInt().toString() else weightVal.toString()
                } else ""
                val repsStr = if (repsVal > 0) repsVal.toString() else ""

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(0.2.dp, BorderDark)
                        .padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "S${setIndex + 1}", fontSize = 9.sp, color = NeonPurple)

                    MiniNumberInput(
                        value = weightStr,
                        unit = "kg",
                        onValueChange = { newStr ->
                            val parsedWeight = newStr.toDoubleOrNull() ?: 0.0
                            onSetUpdate(setIndex, parsedWeight, repsVal)
                        }
                    )

                    MiniNumberInput(
                        value = repsStr,
                        unit = "powt",
                        onValueChange = { newStr ->
                            val parsedReps = newStr.toIntOrNull() ?: 0
                            onSetUpdate(setIndex, weightVal, parsedReps)

                            if (parsedReps > 0) {
                                RestTimerManager.startTimer(context, 3 * 1000L)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniNumberInput(
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .width(52.dp)
            .background(DarkSurface)
            .border(0.5.dp, BorderDark)
            .padding(vertical = 3.dp, horizontal = 1.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = {
                if (it.length <= 4) {
                    text = it
                    onValueChange(it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(AccentGreen),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = AccentGreen
            ),
            modifier = Modifier.widthIn(min = 10.dp, max = 28.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (text.isEmpty()) {
                        Text(text = unit, fontSize = 9.sp, color = TextMuted)
                    }
                    innerTextField()
                }
            }
        )
        if (text.isNotEmpty() && unit == "kg") {
            Text(
                text = "kg",
                fontSize = 8.sp,
                color = TextMuted,
                modifier = Modifier.padding(start = 1.dp)
            )
        }
    }

}

// ============================================
// OKNO DIALOGOWE: BEZPOŚREDNIA EDYCJA ĆWICZENIA
// ============================================
@Composable
fun EditExerciseDialog(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, targetReps: Int, targetSets: Int) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var targetRepsStr by remember {
        mutableStateOf(if (exercise.targetRepetitions > 0) exercise.targetRepetitions.toString() else "")
    }
    var targetSetsStr by remember {
        mutableStateOf(if (exercise.targetSets > 0) exercise.targetSets.toString() else "3")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceHeader,
        title = {
            Text(
                text = "Edytuj ćwiczenie",
                color = AccentGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa ćwiczenia", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = AccentGreen
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetSetsStr,
                        onValueChange = { targetSetsStr = it },
                        label = { Text("Serie", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = AccentGreen
                        )
                    )
                    OutlinedTextField(
                        value = targetRepsStr,
                        onValueChange = { targetRepsStr = it },
                        label = { Text("Powtórzenia", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = AccentGreen
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedReps = targetRepsStr.toIntOrNull() ?: exercise.targetRepetitions
                        val parsedSets = targetSetsStr.toIntOrNull() ?: exercise.targetSets
                        onSave(name, parsedReps, parsedSets)
                    }
                }
            ) {
                Text("Zapisz", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Usuń z planu", color = DangerRed, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Anuluj", color = TextMuted)
                }
            }
        }
    )
}