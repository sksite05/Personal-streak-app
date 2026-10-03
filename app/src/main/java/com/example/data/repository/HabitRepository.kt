package com.example.data.repository

import com.example.data.dao.HabitDao
import com.example.data.dao.RewardDao
import com.example.data.dao.SettingsDao
import com.example.data.model.CustomReward
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.UserSettings
import com.example.util.Milestone
import com.example.util.MilestoneDefinitions
import com.example.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class HabitRepository(
    private val habitDao: HabitDao,
    private val rewardDao: RewardDao,
    private val settingsDao: SettingsDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val mainHabitFlow: Flow<Habit?> = habitDao.getMainHabitFlow()
    val customRewards: Flow<List<CustomReward>> = rewardDao.getCustomRewards()
    val settingsFlow: Flow<UserSettings?> = settingsDao.getSettings()

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = habitDao.getLogsForHabit(habitId)

    suspend fun getMainHabit(): Habit? = habitDao.getMainHabit()

    suspend fun getOrCreateMainHabit(): Habit {
        val existing = habitDao.getMainHabit()
        if (existing != null) return existing

        val allHabits = habitDao.getAllHabitsSync()
        if (allHabits.isNotEmpty()) {
            val first = allHabits.first()
            habitDao.clearMainHabits()
            habitDao.setMainHabit(first.id)
            return first.copy(isMainHabit = true)
        }

        val newMain = Habit(
            name = "Daily Reading & Focus",
            emoji = "⚡",
            colorHex = "#F97316",
            reminderTime = "08:30",
            isMainHabit = true
        )
        val id = habitDao.insertHabit(newMain)
        return newMain.copy(id = id)
    }

    suspend fun markHabitDone(
        habitId: Long,
        dateString: String = StreakCalculator.getTodayString()
    ): Milestone? {
        val existing = habitDao.getLogForDate(habitId, dateString)
        if (existing == null) {
            habitDao.insertLog(
                HabitLog(
                    habitId = habitId,
                    dateString = dateString,
                    completedAt = System.currentTimeMillis()
                )
            )
        }

        // Recalculate streak
        val logs = habitDao.getLogsListForHabit(habitId)
        val settings = settingsDao.getSettingsSync() ?: UserSettings()
        val streakResult = StreakCalculator.calculateStreaks(
            logs = logs,
            hasAvailableFreeze = settings.streakFreezesCount > 0
        )

        val habit = habitDao.getHabitById(habitId)
        var hitMilestone: Milestone? = null

        if (habit != null) {
            val updated = habit.copy(
                currentStreak = streakResult.currentStreak,
                longestStreak = streakResult.longestStreak,
                totalCompletions = streakResult.totalCompletions
            )
            habitDao.updateHabit(updated)

            if (existing == null) {
                // Check if a milestone was reached with currentStreak
                val milestone = MilestoneDefinitions.ALL_MILESTONES.find { it.days == streakResult.currentStreak }
                if (milestone != null) {
                    hitMilestone = milestone
                    if (milestone.bonusFreezeEarned) {
                        settingsDao.insertOrUpdate(
                            settings.copy(
                                streakFreezesCount = settings.streakFreezesCount + 1,
                                totalXp = settings.totalXp + 150
                            )
                        )
                    }
                } else {
                    // Award XP: base 50 + streak bonus (streak * 5)
                    val earnedXp = 50 + (streakResult.currentStreak * 5)
                    settingsDao.insertOrUpdate(
                        settings.copy(totalXp = settings.totalXp + earnedXp)
                    )
                }
            }
        }

        return hitMilestone
    }

    suspend fun unmarkHabitDone(habitId: Long, dateString: String) {
        habitDao.deleteLogForDate(habitId, dateString)
        val logs = habitDao.getLogsListForHabit(habitId)
        val settings = settingsDao.getSettingsSync() ?: UserSettings()
        val streakResult = StreakCalculator.calculateStreaks(logs, settings.streakFreezesCount > 0)
        val habit = habitDao.getHabitById(habitId)
        if (habit != null) {
            habitDao.updateHabit(
                habit.copy(
                    currentStreak = streakResult.currentStreak,
                    longestStreak = streakResult.longestStreak,
                    totalCompletions = streakResult.totalCompletions
                )
            )
        }
    }

    suspend fun useStreakFreeze(habitId: Long): Boolean {
        val settings = settingsDao.getSettingsSync() ?: return false
        if (settings.streakFreezesCount <= 0) return false

        val yesterdayStr = StreakCalculator.getYesterdayString()
        val existing = habitDao.getLogForDate(habitId, yesterdayStr)
        if (existing != null) return false

        // Apply freeze log for yesterday
        habitDao.insertLog(
            HabitLog(
                habitId = habitId,
                dateString = yesterdayStr,
                completedAt = System.currentTimeMillis(),
                isFreezeUsed = true
            )
        )

        // Decrement freeze
        settingsDao.insertOrUpdate(
            settings.copy(streakFreezesCount = settings.streakFreezesCount - 1)
        )

        // Recalculate
        val logs = habitDao.getLogsListForHabit(habitId)
        val streakResult = StreakCalculator.calculateStreaks(logs, false)
        val habit = habitDao.getHabitById(habitId)
        if (habit != null) {
            habitDao.updateHabit(
                habit.copy(
                    currentStreak = streakResult.currentStreak,
                    longestStreak = streakResult.longestStreak,
                    totalCompletions = streakResult.totalCompletions
                )
            )
        }
        return true
    }

    suspend fun setMainHabit(id: Long) {
        habitDao.clearMainHabits()
        habitDao.setMainHabit(id)
    }

    suspend fun createHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(id: Long) {
        habitDao.deleteHabit(id)
        habitDao.deleteLogsForHabit(id)
    }

    suspend fun addCustomReward(reward: CustomReward) {
        rewardDao.insertCustomReward(reward)
    }

    suspend fun claimReward(rewardId: Long) {
        val list = rewardDao.getCustomRewardsList()
        val found = list.find { it.id == rewardId } ?: return
        rewardDao.updateCustomReward(
            found.copy(isClaimed = true, claimedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteReward(id: Long) {
        rewardDao.deleteCustomReward(id)
    }
}
