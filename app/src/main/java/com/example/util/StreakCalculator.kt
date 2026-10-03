package com.example.util

import com.example.data.model.HabitLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StreakCalculator {
    private fun getDateFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayString(): String = getDateFormat().format(Date())

    fun getYesterdayString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return getDateFormat().format(cal.time)
    }

    fun getDateDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return getDateFormat().format(cal.time)
    }

    data class StreakResult(
        val currentStreak: Int,
        val longestStreak: Int,
        val totalCompletions: Int,
        val isTodayCompleted: Boolean,
        val isYesterdayCompleted: Boolean,
        val streakAtRisk: Boolean
    )

    fun calculateStreaks(
        logs: List<HabitLog>,
        hasAvailableFreeze: Boolean = false
    ): StreakResult {
        if (logs.isEmpty()) {
            return StreakResult(
                currentStreak = 0,
                longestStreak = 0,
                totalCompletions = 0,
                isTodayCompleted = false,
                isYesterdayCompleted = false,
                streakAtRisk = false
            )
        }

        val df = getDateFormat()
        val logMap = logs.associateBy { it.dateString }
        val todayStr = getTodayString()
        val yesterdayStr = getYesterdayString()

        val isTodayDone = logMap.containsKey(todayStr)
        val isYesterdayDone = logMap.containsKey(yesterdayStr)

        // Calculate Current Streak
        var currentStreak = 0

        // If today is completed, start counting backwards from today
        // If not completed, start counting from yesterday
        var startOffset = 0
        if (!isTodayDone) {
            if (isYesterdayDone) {
                startOffset = 1
            } else {
                startOffset = 0
            }
        }

        var usedFreezeInCurrentCount = false
        var consecutive = 0
        val loopCal = Calendar.getInstance()
        loopCal.add(Calendar.DAY_OF_YEAR, -startOffset)

        while (true) {
            val dateStr = df.format(loopCal.time)
            val log = logMap[dateStr]

            if (log != null) {
                consecutive++
            } else {
                // Check if freeze can protect 1 missed day
                if (!usedFreezeInCurrentCount && hasAvailableFreeze && consecutive > 0) {
                    usedFreezeInCurrentCount = true
                    // count continues across 1 day gap
                } else {
                    break
                }
            }
            loopCal.add(Calendar.DAY_OF_YEAR, -1)
            if (consecutive > 1000) break // sanity bound
        }
        currentStreak = consecutive

        // Calculate Longest Streak by parsing all unique dates
        val sortedDates = logs.mapNotNull {
            try { df.parse(it.dateString) } catch (e: Exception) { null }
        }.map { d ->
            val c = Calendar.getInstance().apply {
                time = d
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            c.timeInMillis
        }.distinct().sorted()

        var maxStreak = 0
        var tempStreak = 0
        var prevTime: Long? = null
        val ONE_DAY_MS = 24 * 60 * 60 * 1000L

        for (timeMs in sortedDates) {
            if (prevTime == null) {
                tempStreak = 1
            } else {
                val diffDays = ((timeMs - prevTime) / ONE_DAY_MS).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else if (diffDays == 0) {
                    // duplicate date
                } else {
                    tempStreak = 1
                }
            }
            if (tempStreak > maxStreak) {
                maxStreak = tempStreak
            }
            prevTime = timeMs
        }

        val longest = maxOf(maxStreak, currentStreak)
        val streakAtRisk = !isTodayDone && (isYesterdayDone || currentStreak > 0)

        return StreakResult(
            currentStreak = currentStreak,
            longestStreak = longest,
            totalCompletions = logs.size,
            isTodayCompleted = isTodayDone,
            isYesterdayCompleted = isYesterdayDone,
            streakAtRisk = streakAtRisk
        )
    }
}
