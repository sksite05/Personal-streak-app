package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.CustomReward
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.UserSettings
import com.example.data.model.WaterLog
import com.example.data.repository.HabitRepository
import com.example.data.repository.WaterProgressInfo
import com.example.data.repository.WaterRepository
import com.example.data.repository.WaterScheduleSlot
import com.example.util.BackupUtil
import com.example.util.HapticType
import com.example.util.HapticUtil
import com.example.util.Milestone
import com.example.util.MilestoneDefinitions
import com.example.util.NotificationHelper
import com.example.util.SoundUtil
import com.example.util.StreakCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val habitRepository = HabitRepository(database.habitDao(), database.rewardDao(), database.settingsDao())
    val waterRepository = WaterRepository(database.waterDao(), database.settingsDao())

    val allHabits: StateFlow<List<Habit>> = habitRepository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mainHabit: StateFlow<Habit?> = habitRepository.mainHabitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userSettings: StateFlow<UserSettings> = habitRepository.settingsFlow
        .combine(MutableStateFlow(UserSettings())) { settings, default ->
            settings ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val customRewards: StateFlow<List<CustomReward>> = habitRepository.customRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedHabitId = MutableStateFlow<Long?>(null)
    val selectedHabitId: StateFlow<Long?> = _selectedHabitId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val habitLogs: StateFlow<List<HabitLog>> = mainHabit
        .flatMapLatest { habit ->
            val habitId = _selectedHabitId.value ?: habit?.id
            if (habitId != null) {
                habitRepository.getLogsForHabit(habitId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterLogsToday: StateFlow<List<WaterLog>> = waterRepository.getLogsForDate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWaterLogs: StateFlow<List<WaterLog>> = waterRepository.allWaterLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _celebrationMilestone = MutableStateFlow<Milestone?>(null)
    val celebrationMilestone: StateFlow<Milestone?> = _celebrationMilestone.asStateFlow()

    private val _confettiTrigger = MutableStateFlow(false)
    val confettiTrigger: StateFlow<Boolean> = _confettiTrigger.asStateFlow()

    init {
        NotificationHelper.createNotificationChannels(application)
        viewModelScope.launch {
            val habit = habitRepository.getOrCreateMainHabit()
            _selectedHabitId.value = habit.id
        }
    }

    fun selectHabit(habitId: Long) {
        _selectedHabitId.value = habitId
    }

    fun markTodayDone() {
        viewModelScope.launch {
            val habit = mainHabit.value 
                ?: habitRepository.getMainHabit() 
                ?: habitRepository.getOrCreateMainHabit()
            val milestone = habitRepository.markHabitDone(habit.id, StreakCalculator.getTodayString())
            _confettiTrigger.value = true
            val settings = userSettings.value
            HapticUtil.performHaptic(getApplication(), HapticType.HEAVY, settings.hapticsEnabled)
            SoundUtil.playDoneSound(settings.soundEnabled)

            if (milestone != null) {
                _celebrationMilestone.value = milestone
            }
        }
    }

    fun markYesterdayDone() {
        viewModelScope.launch {
            val habit = mainHabit.value 
                ?: habitRepository.getMainHabit() 
                ?: habitRepository.getOrCreateMainHabit()
            val yesterdayStr = StreakCalculator.getYesterdayString()
            val milestone = habitRepository.markHabitDone(habit.id, yesterdayStr)
            _confettiTrigger.value = true
            val settings = userSettings.value
            HapticUtil.performHaptic(getApplication(), HapticType.MEDIUM, settings.hapticsEnabled)
            SoundUtil.playDoneSound(settings.soundEnabled)
            if (milestone != null) {
                _celebrationMilestone.value = milestone
            }
        }
    }

    fun unmarkTodayDone() {
        viewModelScope.launch {
            val habit = mainHabit.value 
                ?: habitRepository.getMainHabit() 
                ?: habitRepository.getOrCreateMainHabit()
            habitRepository.unmarkHabitDone(habit.id, StreakCalculator.getTodayString())
        }
    }

    fun useFreeze() {
        viewModelScope.launch {
            val habit = mainHabit.value 
                ?: habitRepository.getMainHabit() 
                ?: habitRepository.getOrCreateMainHabit()
            val success = habitRepository.useStreakFreeze(habit.id)
            if (success) {
                HapticUtil.performHaptic(getApplication(), HapticType.MEDIUM, userSettings.value.hapticsEnabled)
            }
        }
    }

    fun dismissCelebration() {
        _celebrationMilestone.value = null
    }

    fun resetConfetti() {
        _confettiTrigger.value = false
    }

    // Water Tracker methods
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.addWater(amountMl)
            val settings = userSettings.value
            HapticUtil.performHaptic(getApplication(), HapticType.LIGHT, settings.hapticsEnabled)
            SoundUtil.playSipSound(settings.soundEnabled)
        }
    }

    fun setBottleWaterLevel(targetMl: Int) {
        viewModelScope.launch {
            waterRepository.setBottleLevel(targetMl)
            val settings = userSettings.value
            HapticUtil.performHaptic(getApplication(), HapticType.LIGHT, settings.hapticsEnabled)
            SoundUtil.playSipSound(settings.soundEnabled)
        }
    }

    fun undoLastSip() {
        viewModelScope.launch {
            waterRepository.undoLastSip()
            HapticUtil.performHaptic(getApplication(), HapticType.LIGHT, userSettings.value.hapticsEnabled)
        }
    }

    fun getWaterProgressInfo(): WaterProgressInfo {
        return waterRepository.calculateProgressInfo(
            todayLogs = waterLogsToday.value,
            allLogs = allWaterLogs.value,
            settings = userSettings.value
        )
    }

    fun getWaterScheduleSlots(): List<WaterScheduleSlot> {
        return waterRepository.generateScheduleSlots(userSettings.value)
    }

    // Habits Management
    fun createHabit(name: String, emoji: String, colorHex: String, reminderTime: String) {
        viewModelScope.launch {
            val newHabit = Habit(
                name = name,
                emoji = emoji,
                colorHex = colorHex,
                reminderTime = reminderTime,
                isMainHabit = allHabits.value.isEmpty()
            )
            habitRepository.createHabit(newHabit)
        }
    }

    fun setAsMainHabit(id: Long) {
        viewModelScope.launch {
            habitRepository.setMainHabit(id)
            selectHabit(id)
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            habitRepository.deleteHabit(id)
        }
    }

    // Rewards Management
    fun addCustomReward(targetDays: Int, title: String) {
        viewModelScope.launch {
            val main = mainHabit.value
            habitRepository.addCustomReward(
                CustomReward(
                    habitId = main?.id ?: 0,
                    targetDays = targetDays,
                    title = title
                )
            )
        }
    }

    fun claimCustomReward(rewardId: Long) {
        viewModelScope.launch {
            habitRepository.claimReward(rewardId)
            HapticUtil.performHaptic(getApplication(), HapticType.CELEBRATION, userSettings.value.hapticsEnabled)
            SoundUtil.playMilestoneCelebration(userSettings.value.soundEnabled)
            _confettiTrigger.value = true
        }
    }

    fun deleteCustomReward(rewardId: Long) {
        viewModelScope.launch {
            habitRepository.deleteReward(rewardId)
        }
    }

    // Settings
    fun updateSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            database.settingsDao().insertOrUpdate(newSettings)
        }
    }

    suspend fun exportJsonBackup(): String {
        return BackupUtil.exportToJson(database)
    }

    suspend fun importJsonBackup(json: String): Boolean {
        return BackupUtil.importFromJson(database, json)
    }
}
