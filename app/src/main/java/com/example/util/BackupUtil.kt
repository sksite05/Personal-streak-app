package com.example.util

import com.example.data.AppDatabase
import com.example.data.model.CustomReward
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.UserSettings
import com.example.data.model.WaterLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object BackupUtil {

    suspend fun exportToJson(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTime", System.currentTimeMillis())

        // Habits
        val habitsArray = JSONArray()
        val allHabits = database.habitDao().getAllHabits()
        // We can collect or query synchronously
        val habits = database.habitDao().getAllHabitsSync()
        for (h in habits) {
            val hObj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("emoji", h.emoji)
                put("colorHex", h.colorHex)
                put("reminderTime", h.reminderTime)
                put("isMainHabit", h.isMainHabit)
                put("currentStreak", h.currentStreak)
                put("longestStreak", h.longestStreak)
                put("totalCompletions", h.totalCompletions)
                put("createdAt", h.createdAt)
            }
            habitsArray.put(hObj)
        }
        root.put("habits", habitsArray)

        // Habit Logs
        val logsArray = JSONArray()
        val logs = database.habitDao().getAllLogsList()
        for (l in logs) {
            val lObj = JSONObject().apply {
                put("id", l.id)
                put("habitId", l.habitId)
                put("dateString", l.dateString)
                put("completedAt", l.completedAt)
                put("isFreezeUsed", l.isFreezeUsed)
            }
            logsArray.put(lObj)
        }
        root.put("habitLogs", logsArray)

        // Water Logs
        val waterArray = JSONArray()
        val waterLogs = database.waterDao().getAllWaterLogsList()
        for (w in waterLogs) {
            val wObj = JSONObject().apply {
                put("id", w.id)
                put("dateString", w.dateString)
                put("amountMl", w.amountMl)
                put("timestamp", w.timestamp)
            }
            waterArray.put(wObj)
        }
        root.put("waterLogs", waterArray)

        // Rewards
        val rewardsArray = JSONArray()
        val rewards = database.rewardDao().getCustomRewardsList()
        for (r in rewards) {
            val rObj = JSONObject().apply {
                put("id", r.id)
                put("habitId", r.habitId)
                put("targetDays", r.targetDays)
                put("title", r.title)
                put("isClaimed", r.isClaimed)
                put("claimedAt", r.claimedAt ?: 0L)
            }
            rewardsArray.put(rObj)
        }
        root.put("customRewards", rewardsArray)

        // Settings
        val settings = database.settingsDao().getSettingsSync()
        if (settings != null) {
            val sObj = JSONObject().apply {
                put("wakeTime", settings.wakeTime)
                put("sleepTime", settings.sleepTime)
                put("dailyWaterGoalMl", settings.dailyWaterGoalMl)
                put("streakFreezesCount", settings.streakFreezesCount)
                put("totalXp", settings.totalXp)
                put("soundEnabled", settings.soundEnabled)
                put("hapticsEnabled", settings.hapticsEnabled)
                put("notificationsEnabled", settings.notificationsEnabled)
            }
            root.put("settings", sObj)
        }

        root.toString(2)
    }

    suspend fun importFromJson(database: AppDatabase, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("habits")) {
                val habitsArr = root.getJSONArray("habits")
                for (i in 0 until habitsArr.length()) {
                    val obj = habitsArr.getJSONObject(i)
                    database.habitDao().insertHabit(
                        Habit(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            emoji = obj.optString("emoji", "🔥"),
                            colorHex = obj.optString("colorHex", "#F97316"),
                            reminderTime = obj.optString("reminderTime", "09:00"),
                            isMainHabit = obj.optBoolean("isMainHabit", false),
                            currentStreak = obj.optInt("currentStreak", 0),
                            longestStreak = obj.optInt("longestStreak", 0),
                            totalCompletions = obj.optInt("totalCompletions", 0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("habitLogs")) {
                val logsArr = root.getJSONArray("habitLogs")
                for (i in 0 until logsArr.length()) {
                    val obj = logsArr.getJSONObject(i)
                    database.habitDao().insertLog(
                        HabitLog(
                            id = obj.optLong("id", 0),
                            habitId = obj.getLong("habitId"),
                            dateString = obj.getString("dateString"),
                            completedAt = obj.optLong("completedAt", System.currentTimeMillis()),
                            isFreezeUsed = obj.optBoolean("isFreezeUsed", false)
                        )
                    )
                }
            }

            if (root.has("waterLogs")) {
                val waterArr = root.getJSONArray("waterLogs")
                for (i in 0 until waterArr.length()) {
                    val obj = waterArr.getJSONObject(i)
                    database.waterDao().insertWaterLog(
                        WaterLog(
                            id = obj.optLong("id", 0),
                            dateString = obj.getString("dateString"),
                            amountMl = obj.getInt("amountMl"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("customRewards")) {
                val rArr = root.getJSONArray("customRewards")
                for (i in 0 until rArr.length()) {
                    val obj = rArr.getJSONObject(i)
                    database.rewardDao().insertCustomReward(
                        CustomReward(
                            id = obj.optLong("id", 0),
                            habitId = obj.optLong("habitId", 0),
                            targetDays = obj.getInt("targetDays"),
                            title = obj.getString("title"),
                            isClaimed = obj.optBoolean("isClaimed", false),
                            claimedAt = if (obj.has("claimedAt") && obj.getLong("claimedAt") > 0) obj.getLong("claimedAt") else null
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
