package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.HabitDao
import com.example.data.dao.RewardDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.WaterDao
import com.example.data.model.CustomReward
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.UserSettings
import com.example.data.model.WaterLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Habit::class,
        HabitLog::class,
        WaterLog::class,
        CustomReward::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun waterDao(): WaterDao
    abstract fun rewardDao(): RewardDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "streaksip.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial data asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val existing = db.habitDao().getAllHabitsSync()
            if (existing.isNotEmpty()) return

            // Seed main habit
            val habitId = db.habitDao().insertHabit(
                Habit(
                    name = "Daily Reading & Focus",
                    emoji = "⚡",
                    colorHex = "#F97316",
                    reminderTime = "09:00",
                    isMainHabit = true,
                    currentStreak = 0,
                    longestStreak = 0,
                    totalCompletions = 0
                )
            )

            // Seed user settings
            db.settingsDao().insertOrUpdate(
                UserSettings(
                    id = 1,
                    wakeTime = "07:00",
                    sleepTime = "22:00",
                    dailyWaterGoalMl = 2000,
                    bottleCapacityMl = 1000,
                    streakFreezesCount = 2,
                    totalXp = 50,
                    waterFeedsHabitStreak = true,
                    soundEnabled = true,
                    hapticsEnabled = true,
                    notificationsEnabled = true,
                    isOnboardingCompleted = true,
                    customScheduleCsv = "08:00,11:00,14:00,17:00,20:00"
                )
            )

            // Seed default rewards
            db.rewardDao().insertCustomReward(
                CustomReward(
                    habitId = habitId,
                    targetDays = 3,
                    title = "Treat yourself to specialty coffee ☕",
                    isClaimed = false
                )
            )
            db.rewardDao().insertCustomReward(
                CustomReward(
                    habitId = habitId,
                    targetDays = 7,
                    title = "Watch weekend movie marathon 🎬",
                    isClaimed = false
                )
            )
            db.rewardDao().insertCustomReward(
                CustomReward(
                    habitId = habitId,
                    targetDays = 21,
                    title = "Dinner at favorite restaurant 🍣",
                    isClaimed = false
                )
            )
            db.rewardDao().insertCustomReward(
                CustomReward(
                    habitId = habitId,
                    targetDays = 30,
                    title = "Buy the wishlist gadget 🎧",
                    isClaimed = false
                )
            )
        }
    }
}
