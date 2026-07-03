package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import platform.Foundation.NSUserDefaults
import platform.Foundation.setInteger
import platform.Foundation.integerForKey

class IosGameScoreRepository : GameScoreRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val scoresFlow = MutableStateFlow<List<GameScore>>(emptyList())

    init {
        loadScores()
    }

    private fun loadScores() {
        val snake = defaults.integerForKey("score_SNAKE").toInt()
        val ttt = defaults.integerForKey("score_TIC_TAC_TOE").toInt()
        val memory = defaults.integerForKey("score_MEMORY").toInt()
        val sudoku = defaults.integerForKey("score_SUDOKU").toInt()

        val list = mutableListOf<GameScore>()
        if (snake > 0) list.add(GameScore(1, "SNAKE", snake, 0L))
        if (ttt > 0) list.add(GameScore(2, "TIC_TAC_TOE", ttt, 0L))
        if (memory > 0) list.add(GameScore(3, "MEMORY", memory, 0L))
        if (sudoku > 0) list.add(GameScore(4, "SUDOKU", sudoku, 0L))
        scoresFlow.value = list
    }

    override fun getHighScore(gameType: String): Flow<Int> {
        return scoresFlow.map { list ->
            list.firstOrNull { it.gameType == gameType }?.score ?: 0
        }
    }

    override fun getScoresByGameType(gameType: String): Flow<List<GameScore>> {
        return scoresFlow.map { list ->
            list.filter { it.gameType == gameType }
        }
    }

    override fun getAllScores(): Flow<List<GameScore>> {
        return scoresFlow
    }

    override suspend fun insertScore(gameType: String, score: Int) {
        val currentMax = defaults.integerForKey("score_$gameType").toInt()
        if (score > currentMax) {
            defaults.setInteger(score.toLong(), "score_$gameType")
            loadScores()
        }
    }

    override suspend fun clearScoresByGameType(gameType: String) {
        defaults.setInteger(0L, "score_$gameType")
        loadScores()
    }

    override suspend fun clearAllScores() {
        defaults.setInteger(0L, "score_SNAKE")
        defaults.setInteger(0L, "score_TIC_TAC_TOE")
        defaults.setInteger(0L, "score_MEMORY")
        defaults.setInteger(0L, "score_SUDOKU")
        loadScores()
    }
}
