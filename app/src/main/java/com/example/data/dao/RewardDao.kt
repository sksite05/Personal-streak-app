package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomReward
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM custom_rewards ORDER BY targetDays ASC")
    fun getCustomRewards(): Flow<List<CustomReward>>

    @Query("SELECT * FROM custom_rewards ORDER BY targetDays ASC")
    suspend fun getCustomRewardsList(): List<CustomReward>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomReward(reward: CustomReward): Long

    @Update
    suspend fun updateCustomReward(reward: CustomReward)

    @Query("DELETE FROM custom_rewards WHERE id = :id")
    suspend fun deleteCustomReward(id: Long)
}
