package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY isMainHabit DESC, createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY isMainHabit DESC, createdAt ASC")
    suspend fun getAllHabitsSync(): List<Habit>

    @Query("SELECT * FROM habits ORDER BY isMainHabit DESC, id ASC LIMIT 1")
    fun getMainHabitFlow(): Flow<Habit?>

    @Query("SELECT * FROM habits ORDER BY isMainHabit DESC, id ASC LIMIT 1")
    suspend fun getMainHabit(): Habit?

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("UPDATE habits SET isMainHabit = 0")
    suspend fun clearMainHabits()

    @Query("UPDATE habits SET isMainHabit = 1 WHERE id = :id")
    suspend fun setMainHabit(id: Long)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    // Habit Logs
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString LIMIT 1")
    suspend fun getLogForDate(habitId: Long, dateString: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    suspend fun getLogsListForHabit(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    suspend fun getAllLogsList(): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteLogForDate(habitId: Long, dateString: String)
}
