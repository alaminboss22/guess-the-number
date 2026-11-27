package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GameRecordEntity>>

    @Query("SELECT COALESCE(MAX(score), 0) FROM game_records")
    fun getBestScore(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records")
    fun getTotalGames(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE isWin = 1")
    fun getWinsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecordEntity)

    @Query("DELETE FROM game_records")
    suspend fun clearAllRecords()
}
