package com.example.data.repository

import com.example.data.dao.SettingsDao
import com.example.data.dao.WaterDao
import com.example.data.model.UserSettings
import com.example.data.model.WaterLog
import com.example.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class WaterScheduleSlot(
    val levelMl: Int,
    val timeLabel: String,
    val minutesFromMidnight: Int
)

data class WaterProgressInfo(
    val currentIntakeTodayMl: Int,
    val dailyGoalMl: Int,
    val bottleCapacityMl: Int,
    val bottlesFinishedCount: Int,
    val currentBottleMl: Int,
    val targetLevelNowMl: Int,
    val differenceMl: Int, // positive = ahead, negative = behind
    val isGoalReached: Boolean,
    val currentWaterStreakDays: Int
)

class WaterRepository(
    private val waterDao: WaterDao,
    private val settingsDao: SettingsDao
) {
    val allWaterLogs: Flow<List<WaterLog>> = waterDao.getAllWaterLogs()

    fun getLogsForDate(dateString: String = StreakCalculator.getTodayString()): Flow<List<WaterLog>> =
        waterDao.getLogsForDate(dateString)

    suspend fun addWater(amountMl: Int) {
        val todayStr = StreakCalculator.getTodayString()
        waterDao.insertWaterLog(
            WaterLog(
                dateString = todayStr,
                amountMl = amountMl,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun undoLastSip() {
        val todayStr = StreakCalculator.getTodayString()
        waterDao.deleteLastLogForDate(todayStr)
    }

    suspend fun setBottleLevel(targetLevelMl: Int) {
        val todayStr = StreakCalculator.getTodayString()
        val currentLogs = waterDao.getLogsListForDate(todayStr)
        val currentTotal = currentLogs.sumOf { it.amountMl }
        val settings = settingsDao.getSettingsSync() ?: UserSettings()
        val bottleCapacity = settings.bottleCapacityMl

        // calculate current bottle progress
        val fullBottlesMl = (currentTotal / bottleCapacity) * bottleCapacity
        val newTotal = fullBottlesMl + targetLevelMl.coerceIn(0, bottleCapacity)
        val diff = newTotal - currentTotal

        if (diff > 0) {
            waterDao.insertWaterLog(
                WaterLog(
                    dateString = todayStr,
                    amountMl = diff,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else if (diff < 0) {
            // Adjust by negative or reset entries
            waterDao.insertWaterLog(
                WaterLog(
                    dateString = todayStr,
                    amountMl = diff,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun generateScheduleSlots(settings: UserSettings): List<WaterScheduleSlot> {
        val slots = mutableListOf<WaterScheduleSlot>()
        val custom = settings.customScheduleCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val capacity = settings.bottleCapacityMl

        val levels = listOf(0, capacity / 4, capacity / 2, (capacity * 3) / 4, capacity)

        if (custom.size >= 4) {
            slots.add(WaterScheduleSlot(0, "Start", parseMinutes(settings.wakeTime)))
            for (i in 0 until 4) {
                val lvl = levels[i + 1]
                val time = custom.getOrNull(i) ?: "12:00"
                slots.add(WaterScheduleSlot(lvl, time, parseMinutes(time)))
            }
        } else {
            // Auto calculate between wake and sleep
            val wakeMin = parseMinutes(settings.wakeTime)
            val sleepMin = parseMinutes(settings.sleepTime)
            val totalSpan = (if (sleepMin > wakeMin) sleepMin - wakeMin else 1440 - wakeMin + sleepMin).coerceAtLeast(60)

            slots.add(WaterScheduleSlot(0, settings.wakeTime, wakeMin))
            for (i in 1..4) {
                val lvl = levels[i]
                val slotMin = (wakeMin + (totalSpan * i / 4)) % 1440
                val h = slotMin / 60
                val m = slotMin % 60
                val amPm = if (h >= 12) "PM" else "AM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                val label = String.format("%d:%02d %s", h12, m, amPm)
                slots.add(WaterScheduleSlot(lvl, label, slotMin))
            }
        }
        return slots
    }

    fun calculateProgressInfo(
        todayLogs: List<WaterLog>,
        allLogs: List<WaterLog>,
        settings: UserSettings
    ): WaterProgressInfo {
        val todayIntake = todayLogs.sumOf { it.amountMl }.coerceAtLeast(0)
        val goal = settings.dailyWaterGoalMl
        val capacity = settings.bottleCapacityMl.coerceAtLeast(500)

        val bottlesFinished = todayIntake / capacity
        val currentBottleLevel = todayIntake % capacity

        // Calculate expected target right now
        val cal = Calendar.getInstance()
        val currentMinutesNow = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val wakeMin = parseMinutes(settings.wakeTime)
        val sleepMin = parseMinutes(settings.sleepTime)

        val totalActiveMinutes = (if (sleepMin > wakeMin) sleepMin - wakeMin else 1440 - wakeMin + sleepMin).coerceAtLeast(60)
        val elapsedMinutes = when {
            currentMinutesNow < wakeMin -> 0
            currentMinutesNow > sleepMin && sleepMin > wakeMin -> totalActiveMinutes
            else -> (currentMinutesNow - wakeMin).coerceIn(0, totalActiveMinutes)
        }

        val fractionOfDay = (elapsedMinutes.toFloat() / totalActiveMinutes).coerceIn(0f, 1f)
        val expectedTotalNow = (goal * fractionOfDay).toInt()
        val differenceMl = todayIntake - expectedTotalNow

        // Calculate current bottle target line (where water inside the current 1000ml bottle should be)
        val expectedInBottle = when {
            bottlesFinished >= (goal / capacity) -> capacity
            else -> {
                val targetForBottle = (capacity * fractionOfDay).toInt()
                targetForBottle.coerceIn(0, capacity)
            }
        }

        // Calculate water streak (consecutive days where goal was achieved)
        val groupedByDate = allLogs.groupBy { it.dateString }
        var streak = 0
        var checkCal = Calendar.getInstance()
        val todayStr = StreakCalculator.getTodayString()
        val isTodayGoalMet = todayIntake >= goal

        // If today not met yet, check from yesterday
        if (!isTodayGoalMet) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dateStr = StreakCalculator.getDateDaysAgo(streak + (if (!isTodayGoalMet) 1 else 0))
            val dayTotal = groupedByDate[dateStr]?.sumOf { it.amountMl } ?: 0
            if (dayTotal >= goal) {
                streak++
            } else {
                break
            }
            if (streak > 500) break
        }

        return WaterProgressInfo(
            currentIntakeTodayMl = todayIntake,
            dailyGoalMl = goal,
            bottleCapacityMl = capacity,
            bottlesFinishedCount = bottlesFinished,
            currentBottleMl = currentBottleLevel,
            targetLevelNowMl = expectedInBottle,
            differenceMl = differenceMl,
            isGoalReached = todayIntake >= goal,
            currentWaterStreakDays = streak
        )
    }

    private fun parseMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
        } catch (e: Exception) {
            480 // 8:00 AM fallback
        }
    }
}
