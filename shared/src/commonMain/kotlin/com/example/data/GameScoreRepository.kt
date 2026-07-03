package com.example.data

import kotlinx.coroutines.flow.Flow

interface GameScoreRepository {
    fun getHighScore(gameType: String): Flow<Int>
    fun getScoresByGameType(gameType: String): Flow<List<GameScore>>
    fun getAllScores(): Flow<List<GameScore>>
    suspend fun insertScore(gameType: String, score: Int)
    suspend fun clearScoresByGameType(gameType: String)
    suspend fun clearAllScores()
}
