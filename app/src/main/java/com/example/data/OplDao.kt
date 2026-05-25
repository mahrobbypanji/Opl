package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OplDao {
    @Query("SELECT * FROM opl_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<OplEntry>>

    @Query("SELECT * FROM opl_entries WHERE tanggalString = :dateStr ORDER BY timestamp DESC")
    fun getEntriesForDate(dateStr: String): Flow<List<OplEntry>>

    @Query("SELECT * FROM opl_entries WHERE platNomor LIKE :searchQuery ORDER BY timestamp DESC")
    fun searchByPlatNomor(searchQuery: String): Flow<List<OplEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: OplEntry): Long

    @Update
    suspend fun updateEntry(entry: OplEntry)

    @Delete
    suspend fun deleteEntry(entry: OplEntry)
}
