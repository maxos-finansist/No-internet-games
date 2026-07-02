package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, // "SNAKE", "TIC_TAC_TOE", "MEMORY"
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)
