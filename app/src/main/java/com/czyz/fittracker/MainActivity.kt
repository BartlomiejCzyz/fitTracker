package com.czyz.fittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.czyz.fittracker.database.AppDatabase
import com.czyz.fittracker.ui.theme.FitTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fit_tracker.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val factory = WorkoutViewModelFactory(database)
        val viewModel = ViewModelProvider(this, factory)[WorkoutViewModel::class.java]

        setContent {
            FitTrackerTheme {
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}