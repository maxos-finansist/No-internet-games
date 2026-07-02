package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores WHERE gameType = :gameType ORDER BY score DESC")
    fun getScoresByGameType(gameType: String): Flow<List<GameScore>>

    @Query("SELECT MAX(score) FROM game_scores WHERE gameType = :gameType")
    fun getHighScore(gameType: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScore)

    @Query("DELETE FROM game_scores WHERE gameType = :gameType")
    suspend fun clearScoresByGameType(gameType: String)

    @Query("DELETE FROM game_scores")
    suspend fun clearAllScores()
}
