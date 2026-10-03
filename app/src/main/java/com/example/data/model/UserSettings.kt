package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val wakeTime: String = "07:00",
    val sleepTime: String = "22:00",
    val dailyWaterGoalMl: Int = 2000,
    val bottleCapacityMl: Int = 1000,
    val streakFreezesCount: Int = 2,
    val totalXp: Int = 120,
    val waterFeedsHabitStreak: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val customScheduleCsv: String = "08:00,11:00,14:00,17:00,20:00"
)
