package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // Format: "yyyy-MM-dd"
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
