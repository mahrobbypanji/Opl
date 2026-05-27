package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opl_entries")
data class OplEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cabang: String = "DSO Depok",
    val saName: String = "Bunyamin",
    val tanggalString: String, // String format: yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val platNomor: String,
    val typeKendaraan: String = "", 
    val tipePekerjaan: String, // Comma-separated work types (or single now)
    val catatan: String = "" // Additional remarks/notes
)
