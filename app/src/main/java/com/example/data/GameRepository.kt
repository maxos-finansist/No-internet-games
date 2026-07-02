package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameScoreDao) {

    fun getHighScore(gameType: String): Flow<Int> {
        return dao.getHighScore(gameType).map { it ?: 0 }
    }

    fun getScoresByGameType(gameType: String): Flow<List<GameScore>> {
        return dao.getScoresByGameType(gameType)
    }

    fun getAllScores(): Flow<List<GameScore>> {
        return dao.getAllScores()
    }

    suspend fun insertScore(gameType: String, score: Int) {
        dao.insertScore(GameScore(gameType = gameType, score = score))
    }

    suspend fun clearScoresByGameType(gameType: String) {
        dao.clearScoresByGameType(gameType)
    }

    suspend fun clearAllScores() {
        dao.clearAllScores()
    }
}
