package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_ID_HABITS = "channel_streaks"
    const val CHANNEL_ID_WATER = "channel_water"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val habitChannel = NotificationChannel(
                CHANNEL_ID_HABITS,
                "Streak & Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders and streak at risk warnings"
                enableVibration(true)
            }

            val waterChannel = NotificationChannel(
                CHANNEL_ID_WATER,
                "Water Intake Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Hydration timeline nudges"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(habitChannel)
            manager.createNotificationChannel(waterChannel)
        }
    }

    fun showStreakAtRiskNotification(context: Context, habitName: String, streakCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_HABITS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🔥 Your $streakCount-Day Streak is at Risk!")
            .setContentText("Don't let '$habitName' break! Tap now to complete it for today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showWaterNudgeNotification(context: Context, behindAmountMl: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WATER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("💧 Time for a Sip!")
            .setContentText("You're ${behindAmountMl}ml behind your schedule. Take a fresh glass of water now!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1002, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
