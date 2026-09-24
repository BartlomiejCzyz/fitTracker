package com.czyz.fittracker.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.czyz.fittracker.MainActivity
import kotlin.apply

class RestTimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            ACTION_TIMER_EXPIRED -> {
                showRestFinishedNotification(context, notificationManager)
            }
            ACTION_ADD_MINUTE -> {
                notificationManager.cancel(NOTIFICATION_ID)
                // Dodajemy minutę (60 000 ms)
                RestTimerManager.startTimer(context, 60_000L)
            }
            ACTION_DISMISS -> {
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }
    }

    private fun showRestFinishedNotification(context: Context, manager: NotificationManager) {
        createSilentChannel(manager)

        // Intenty dla przycisków
        val addMinuteIntent = Intent(context, RestTimerReceiver::class.java).apply {
            action = ACTION_ADD_MINUTE
        }
        val addMinutePending = PendingIntent.getBroadcast(
            context, 1, addMinuteIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, RestTimerReceiver::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPending = PendingIntent.getBroadcast(
            context, 2, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Koniec przerwy!")
            .setContentText("Czas na kolejną serię.")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Wyświetla baner na górze
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(0) // Brak wibracji i światła
            .setSound(null) // ABSOLUTNIE BRAK DŹWIĘKU
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "+1 Minuta", addMinutePending)
            .addAction(0, "Zamknij", dismissPending)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createSilentChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Przerwy",
                NotificationManager.IMPORTANCE_HIGH // Wysoki priorytet dla baneru
            ).apply {
                setSound(null, null) // Wyłączenie dźwięku na poziomie kanału
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "rest_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_TIMER_EXPIRED = "com.czyz.fittracker.ACTION_TIMER_EXPIRED"
        const val ACTION_ADD_MINUTE = "com.czyz.fittracker.ACTION_ADD_MINUTE"
        const val ACTION_DISMISS = "com.czyz.fittracker.ACTION_DISMISS"
    }
}