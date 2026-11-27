package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val difficultyName: String,
    val secretNumber: Int,
    val attemptsUsed: Int,
    val maxAttempts: Int,
    val score: Int,
    val isWin: Boolean,
    val guessesCsv: String,
    val timestamp: Long = System.currentTimeMillis()
)
