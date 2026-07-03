package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AndroidGameScoreRepository(context: Context) : GameScoreRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("offline_arcade_scores", Context.MODE_PRIVATE)
    private val scoresFlow = MutableStateFlow<List<GameScore>>(emptyList())

    init {
        loadScores()
    }

    private fun loadScores() {
        val snake = prefs.getInt("score_SNAKE", 0)
        val ttt = prefs.getInt("score_TIC_TAC_TOE", 0)
        val memory = prefs.getInt("score_MEMORY", 0)
        val sudoku = prefs.getInt("score_SUDOKU", 0)

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
        val currentMax = prefs.getInt("score_$gameType", 0)
        if (score > currentMax) {
            prefs.edit().putInt("score_$gameType", score).apply()
            loadScores()
        }
    }

    override suspend fun clearScoresByGameType(gameType: String) {
        prefs.edit().putInt("score_$gameType", 0).apply()
        loadScores()
    }

    override suspend fun clearAllScores() {
        prefs.edit()
            .putInt("score_SNAKE", 0)
            .putInt("score_TIC_TAC_TOE", 0)
            .putInt("score_MEMORY", 0)
            .putInt("score_SUDOKU", 0)
            .apply()
        loadScores()
    }
}
