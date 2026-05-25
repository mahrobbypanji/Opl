package com.example.data

import kotlinx.coroutines.flow.Flow

class OplRepository(private val oplDao: OplDao) {
    val allEntries: Flow<List<OplEntry>> = oplDao.getAllEntries()

    fun getEntriesForDate(dateStr: String): Flow<List<OplEntry>> {
        return oplDao.getEntriesForDate(dateStr)
    }

    fun searchByPlatNomor(query: String): Flow<List<OplEntry>> {
        val formattedQuery = "%$query%"
        return oplDao.searchByPlatNomor(formattedQuery)
    }

    suspend fun insert(entry: OplEntry): Long {
        return oplDao.insertEntry(entry)
    }

    suspend fun update(entry: OplEntry) {
        oplDao.updateEntry(entry)
    }

    suspend fun delete(entry: OplEntry) {
        oplDao.deleteEntry(entry)
    }
}
