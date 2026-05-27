package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.OplEntry
import com.example.data.OplRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryFilter {
    ALL,
    DAILY,   // Today or custom day
    WEEKLY,  // Last 7 days
    MONTHLY, // Current month
    RANGE    // Custom date range
}

data class HistoryFilterState(
    val query: String,
    val filterType: HistoryFilter,
    val filterDate: String,
    val filterStartDate: String,
    val filterEndDate: String,
    val filterJob: String?,
    val filterSa: String?
)

class OplViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OplRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OplRepository(database.oplDao())
    }

    // Navigation State (0: Daily OPL, 1: Pencatatan OPL, 2: OPL Editor, 3: Data OPL)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()
    private var editingSourceTab: Int = 0

    fun setActiveTab(index: Int) {
        if (isEditing.value && index != 2) {
            resetForm()
        }
        _activeTab.value = index
    }

    // Raw database entries
    val allEntries: StateFlow<List<OplEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form inputs state
    var formCabang = MutableStateFlow("DSO Depok")
    var formSaName = MutableStateFlow("Bunyamin")
    var formPlatNomor = MutableStateFlow("")
    var formVehicleType = MutableStateFlow("")
    var formVehicleOther = MutableStateFlow("")
    var formSelectedJobs = MutableStateFlow<Set<String>>(emptySet())
    var formNotes = MutableStateFlow("")
    var formDate = MutableStateFlow(getCurrentDateString())
    
    // Edit Mode State
    var isEditing = MutableStateFlow(false)
    private var editingId: Int? = null

    // History screen filters state
    var historySearchQuery = MutableStateFlow("")
    var historyFilterType = MutableStateFlow(HistoryFilter.ALL)
    var selectedFilterDate = MutableStateFlow(getCurrentDateString()) // yyyy-MM-dd
    var selectedFilterStartDate = MutableStateFlow(getCurrentDateString()) // yyyy-MM-dd
    var selectedFilterEndDate = MutableStateFlow(getCurrentDateString()) // yyyy-MM-dd
    var historyFilterJob = MutableStateFlow<String?>(null)
    var historyFilterSa = MutableStateFlow<String?>(null)

    // Data Target / Insentif
    var targetJobPrices = MutableStateFlow<Map<String, String>>(emptyMap())

    fun updateJobPrice(job: String, price: String) {
        val current = targetJobPrices.value.toMutableMap()
        current[job] = price
        targetJobPrices.value = current
    }

    // Dynamic SAs generated from DB list to allow precise autocomplete/chips filtering in Output View
    val availableSAs: StateFlow<List<String>> = allEntries
        .map { list ->
            list.map { it.saName }.distinct().filter { it.isNotBlank() }.sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Bunyamin")
        )

    // Status Message popup
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Pre-defined job types
    val availableJobs = listOf(
        "Salon Mesin",
        "Salon Kaca",
        "Rematching",
        "Headlamp Treatment",
        "Jasa Engine",
        "Salon Interior",
        "Salon Exterior",
        "Karpet",
        "Ex Banjir",
        "Quick Wax"
    )
    
    val availableVehicles = listOf(
        "Ayla", "Sigra", "Xenia", "Terios", "Rocky", 
        "Sirion", "Luxio", "Gran Max", "Taruna", 
        "Feroza", "Zebra", "Taft", "Others"
    )

    // Today's entries specifically for the "Daily OPL" tab
    val dailyEntries: StateFlow<List<OplEntry>> = allEntries
        .map { list ->
            val todayStr = getCurrentDateString()
            list.filter { it.tanggalString == todayStr }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Create an intermediate flow to combine all 3 date fields into a Triple
    private val dateFilterFlow: Flow<Triple<String, String, String>> = combine(
        selectedFilterDate,
        selectedFilterStartDate,
        selectedFilterEndDate
    ) { date, start, end ->
        Triple(date, start, end)
    }

    // Compose history filters into a single state stream
    private val historyFilterState: Flow<HistoryFilterState> = combine(
        historySearchQuery,
        historyFilterType,
        dateFilterFlow,
        historyFilterJob,
        historyFilterSa
    ) { query, type, dates, job, sa ->
        HistoryFilterState(
            query = query,
            filterType = type,
            filterDate = dates.first,
            filterStartDate = dates.second,
            filterEndDate = dates.third,
            filterJob = job,
            filterSa = sa
        )
    }

    // Filtered database entries for "Data OPL" tab
    val filteredHistoryEntries: StateFlow<List<OplEntry>> = combine(
        allEntries,
        historyFilterState
    ) { list, state ->
        list.filter { entry ->
            // Search filter by License Plate (Plat Nomor)
            val matchesSearch = if (state.query.isBlank()) {
                true
            } else {
                entry.platNomor.contains(state.query, ignoreCase = true)
            }

            // Date constraint filter
            val matchesTime = when (state.filterType) {
                HistoryFilter.ALL -> true
                HistoryFilter.DAILY -> entry.tanggalString == state.filterDate
                HistoryFilter.WEEKLY -> {
                    try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val startDate = inputFormat.parse(state.filterDate) ?: java.util.Date()
                        val cal = java.util.Calendar.getInstance()
                        cal.time = startDate
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 6)
                        val endDateStr = inputFormat.format(cal.time)
                        entry.tanggalString in state.filterDate..endDateStr
                    } catch (e: Exception) {
                        false
                    }
                }
                HistoryFilter.MONTHLY -> {
                    try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val selectedDate = inputFormat.parse(state.filterDate) ?: java.util.Date()
                        val entryDate = inputFormat.parse(entry.tanggalString) ?: java.util.Date()
                        val selectedCal = java.util.Calendar.getInstance().apply { time = selectedDate }
                        val entryCal = java.util.Calendar.getInstance().apply { time = entryDate }
                        selectedCal.get(java.util.Calendar.YEAR) == entryCal.get(java.util.Calendar.YEAR) &&
                        selectedCal.get(java.util.Calendar.MONTH) == entryCal.get(java.util.Calendar.MONTH)
                    } catch (e: Exception) {
                        false
                    }
                }
                HistoryFilter.RANGE -> entry.tanggalString in state.filterStartDate..state.filterEndDate
            }

            // Tipe Pekerjaan Filter
            val matchesJob = if (state.filterJob.isNullOrBlank()) {
                true
            } else {
                entry.tipePekerjaan.contains(state.filterJob, ignoreCase = true)
            }

            // SA Name Filter
            val matchesSa = if (state.filterSa.isNullOrBlank()) {
                true
            } else {
                entry.saName.equals(state.filterSa, ignoreCase = true)
            }

            matchesSearch && matchesTime && matchesJob && matchesSa
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // CHARTS specific filter State
    var chartSelectedMonth = MutableStateFlow(getCurrentDateString().substring(0, 7)) // "yyyy-MM" format

    private val chartEntries: Flow<List<OplEntry>> = combine(allEntries, chartSelectedMonth) { list, monthStr ->
        list.filter { it.tanggalString.startsWith(monthStr) }
    }

    // STATISTICS: job breakdown count with respect to the filtered subset to keep it contextually interactive!
    val jobStatistics: StateFlow<Map<String, Int>> = chartEntries
        .map { list ->
            val stats = mutableMapOf<String, Int>()
            availableJobs.forEach { stats[it] = 0 }
            list.forEach { entry ->
                val jobs = entry.tipePekerjaan.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
                jobs.forEach { job ->
                    stats[job] = (stats[job] ?: 0) + 1
                }
            }
            stats.filter { it.value > 0 } // Filter out zeros for optimal charting layout
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // STATISTICS: trend from time to time map of Date -> Unit count
    val trendStatistics: StateFlow<List<Pair<String, Int>>> = chartEntries
        .map { list ->
            list.groupBy { it.tanggalString }
                .map { (dateStr, unitList) -> dateStr to unitList.size }
                .sortedBy { it.first } // Chronological order
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Clear all filter states at once 
    fun clearAllHistoryFilters() {
        historySearchQuery.value = ""
        historyFilterType.value = HistoryFilter.ALL
        selectedFilterDate.value = getCurrentDateString()
        selectedFilterStartDate.value = getCurrentDateString()
        selectedFilterEndDate.value = getCurrentDateString()
        historyFilterJob.value = null
        historyFilterSa.value = null
    }

    // Actions
    fun onJobSelected(job: String, isSelected: Boolean) {
        val current = formSelectedJobs.value.toMutableSet()
        if (isSelected) {
            current.add(job)
        } else {
            current.remove(job)
        }
        formSelectedJobs.value = current
    }

    fun submitOplForm() {
        val plat = formPlatNomor.value.trim().uppercase(Locale.getDefault())
        if (plat.isBlank()) {
            _statusMessage.value = "Maaf, Plat Nomor Kendaraan harus diisi!"
            return
        }

        val selectedJobsList = formSelectedJobs.value
        if (selectedJobsList.isEmpty()) {
            _statusMessage.value = "Pilih minimal 1 Type pekerjaan!"
            return
        }

        val jobsString = selectedJobsList.joinToString(", ")
        val todayStr = formDate.value

        viewModelScope.launch {
            if (isEditing.value && editingId != null) {
                // Update item
                val existing = allEntries.value.find { it.id == editingId }
                if (existing != null) {
                    val updated = existing.copy(
                        cabang = formCabang.value.trim(),
                        saName = formSaName.value.trim(),
                        platNomor = plat,
                        typeKendaraan = if (formVehicleType.value == "Others") formVehicleOther.value.trim() else formVehicleType.value,
                        tipePekerjaan = jobsString,
                        catatan = formNotes.value.trim(),
                        tanggalString = todayStr
                    )
                    repository.update(updated)
                    _statusMessage.value = "Data Perintah Kerja berhasil diperbarui!"
                }
            } else {
                // Insert new item
                val entry = OplEntry(
                    cabang = formCabang.value.trim(),
                    saName = formSaName.value.trim(),
                    tanggalString = todayStr,
                    platNomor = plat,
                    typeKendaraan = if (formVehicleType.value == "Others") formVehicleOther.value.trim() else formVehicleType.value,
                    tipePekerjaan = jobsString,
                    catatan = formNotes.value.trim()
                )
                repository.insert(entry)
                _statusMessage.value = "Data Perintah Kerja ($plat) berhasil disimpan!"
            }

            resetForm()
            // Direct user back to correct list
            _activeTab.value = editingSourceTab
        }
    }

    fun startEditing(entry: OplEntry, sourceTab: Int = 0) {
        formCabang.value = entry.cabang
        formSaName.value = entry.saName
        formPlatNomor.value = entry.platNomor
        formNotes.value = entry.catatan
        formDate.value = entry.tanggalString
        
        if (availableVehicles.contains(entry.typeKendaraan)) {
            formVehicleType.value = entry.typeKendaraan
            formVehicleOther.value = ""
        } else if (entry.typeKendaraan.isNotBlank()) {
            formVehicleType.value = "Others"
            formVehicleOther.value = entry.typeKendaraan
        } else {
            formVehicleType.value = ""
            formVehicleOther.value = ""
        }
        
        // Parse comma-separated list
        val jobsSet = entry.tipePekerjaan.split(", ").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        formSelectedJobs.value = jobsSet
        
        editingId = entry.id
        isEditing.value = true
        editingSourceTab = sourceTab
        _activeTab.value = 2 // Switch to OPL Editor tab
    }

    fun deleteEntry(entry: OplEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            _statusMessage.value = "Data Perintah Kerja untuk ${entry.platNomor} berhasil dihapus."
        }
    }

    fun deleteEntries(entries: List<OplEntry>) {
        viewModelScope.launch {
            entries.forEach { repository.delete(it) }
            _statusMessage.value = "${entries.size} Data Perintah Kerja berhasil dihapus."
        }
    }

    fun cancelEditing() {
        resetForm()
        _activeTab.value = editingSourceTab
    }

    fun resetForm() {
        formPlatNomor.value = ""
        formVehicleType.value = ""
        formVehicleOther.value = ""
        formSelectedJobs.value = emptySet()
        formNotes.value = ""
        formDate.value = getCurrentDateString()
        formCabang.value = "DSO Depok"
        formSaName.value = "Bunyamin"
        isEditing.value = false
        editingId = null
    }

    // Generate Beautiful Demo Data covering historical dates
    fun generateDemoData() {
        viewModelScope.launch {
            val sAs = listOf("Bunyamin", "Sudirman", "Robi", "Yudi")
            val plates = listOf("B 1120 KOD", "B 2489 SCO", "B 1012 Depok", "F 2024 DE", "B 9901 SA", "B 1234 XY", "D 8899 OPL", "B 4752 PT")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            // Today, yesterday, 3 days ago, 5 days ago, 10 days ago, 25 days ago
            val daysOffsets = listOf(0, 0, -1, -2, -4, -6, -10, -15, -28)

            for (i in daysOffsets.indices) {
                val offset = daysOffsets[i]
                val itemCal = Calendar.getInstance()
                itemCal.add(Calendar.DAY_OF_YEAR, offset)
                val dateStr = sdf.format(itemCal.time)
                
                // randomize jobs
                val shuffleJobs = availableJobs.shuffled()
                val jobCount = (1..3).random()
                val selectedList = shuffleJobs.take(jobCount).joinToString(", ")
                
                val demoEntry = OplEntry(
                    cabang = "DSO Depok",
                    saName = sAs.random(),
                    tanggalString = dateStr,
                    timestamp = itemCal.timeInMillis - (i * 3600 * 1000), // separate hourly
                    platNomor = plates.random(),
                    tipePekerjaan = selectedList,
                    catatan = "Demo data OPL - Auto-generated"
                )
                repository.insert(demoEntry)
            }
            _statusMessage.value = "9 Data Demo OPL berhasil ditambahkan!"
        }
    }

    // Helper Date utilities
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun parseDateString(dateStr: String): Date? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun isWithinLast7Days(dateStr: String): Boolean {
        val date = parseDateString(dateStr) ?: return false
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfToday = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -7)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOf7DaysAgo = cal.timeInMillis

        val entryTime = date.time
        return entryTime in startOf7DaysAgo..endOfToday
    }

    private fun isWithinCurrentMonth(dateStr: String): Boolean {
        val date = parseDateString(dateStr) ?: return false
        val calEntry = Calendar.getInstance().apply { time = date }
        val calToday = Calendar.getInstance()
        return calEntry.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
               calEntry.get(Calendar.MONTH) == calToday.get(Calendar.MONTH)
    }
}

class OplViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OplViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OplViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
