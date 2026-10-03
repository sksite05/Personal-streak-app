package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_rewards")
data class CustomReward(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long = 0,
    val targetDays: Int,
    val title: String,
    val isClaimed: Boolean = false,
    val claimedAt: Long? = null
)
