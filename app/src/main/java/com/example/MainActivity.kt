package com.example

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OplEntry
import com.example.ui.BackgroundAnimation
import com.example.ui.HistoryFilter
import com.example.ui.OplViewModel
import com.example.ui.OplViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Initialize modern viewmodel using Factory
                val viewModel: OplViewModel by viewModels {
                    OplViewModelFactory(applicationContext as Application)
                }
                
                OplAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OplAppScreen(viewModel: OplViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    
    // Auto clear toast message after delay
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearStatusMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // High fidelity particle animation background with variant dynamic sync
        BackgroundAnimation(variant = if (activeTab == 3) "v2" else "v1")

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Let the particle canvas shine!
            topBar = {
                OplTopBar(
                    title = "BPK Bengkel",
                    subtitle = "Depok Service Office (DSO)",
                    onGenerateDemo = { viewModel.generateDemoData() }
                )
            },
            bottomBar = {
                OplBottomNavBar(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Screen switching using smooth scale/fade animation
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.96f))
                            .togetherWith(fadeOut() + scaleOut(targetScale = 0.96f))
                    },
                    label = "ScreenTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> DailyOplScreen(viewModel)
                        1 -> PencatatanOplScreen(viewModel)
                        2 -> OplEditorScreen(viewModel)
                        3 -> RiwayatOplScreen(viewModel)
                    }
                }

                // Beautiful status overlay notification toast representing premium UX
                statusMessage?.let { msg ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .shadow(12.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var cur = this
    while (cur is ContextWrapper) {
        if (cur is Activity) return cur
        cur = cur.baseContext
    }
    return null
}

@Composable
fun OplTopBar(
    title: String,
    subtitle: String,
    onGenerateDemo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Elegant OPL Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sleek Gradient Circular Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        tint = Color.White,
                        contentDescription = "OPL Logo",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "OPL WORKSHOP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                            letterSpacing = 1.5.sp
                        )
                        // Live indicator dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Digital Control",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sistem Pemantauan OPL Real-time • $subtitle",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick simulation action button as a sleek pill
            Button(
                onClick = onGenerateDemo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dataset,
                        contentDescription = "Simulasi",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Simulasi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OplBottomNavBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("Daily OPL", fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 0) Icons.Default.Today else Icons.Outlined.Today,
                    contentDescription = "Daily OPL"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Pencatatan", fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 1) Icons.Default.AddBox else Icons.Outlined.AddBox,
                    contentDescription = "Pencatatan OPL"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("OPL Editor", fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 2) Icons.Default.Edit else Icons.Outlined.Edit,
                    contentDescription = "OPL Editor"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = activeTab == 3,
            onClick = { onTabSelected(3) },
            label = { Text("Data OPL", fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 3) Icons.Default.History else Icons.Outlined.History,
                    contentDescription = "Data OPL"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

// ==========================================
// SCREEN 1: DAILY OPL (HARI INI)
// ==========================================
@Composable
fun DailyOplScreen(viewModel: OplViewModel) {
    val entries by viewModel.dailyEntries.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sleek Interface Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily OPL",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Sleek blue progress tracker pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${entries.size} Unit Hari Ini",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Live status indicator line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF22C55E), CircleShape) // Vibrant green dots
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Halaman ini otomatis merefresh untuk tanggal terbaru.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (entries.isEmpty()) {
            EmptyStateContainer(
                message = "Belum ada Perintah Kerja OPL hari ini.\nJika hari sudah berganti, halaman ini akan kosong kembali.",
                actionText = "Input OPL Baru",
                onAction = { viewModel.setActiveTab(1) } // Switch to Input Tab
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries, key = { it.id }) { entry ->
                    OplEntryItemCard(
                        entry = entry,
                        onEdit = { viewModel.startEditing(entry, sourceTab = 0) },
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: PENCATATAN OPL (FORM INPUT)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PencatatanOplScreen(viewModel: OplViewModel) {
    val cabang by viewModel.formCabang.collectAsStateWithLifecycle()
    val saName by viewModel.formSaName.collectAsStateWithLifecycle()
    val platNomor by viewModel.formPlatNomor.collectAsStateWithLifecycle()
    val selectedJobs by viewModel.formSelectedJobs.collectAsStateWithLifecycle()
    val notes by viewModel.formNotes.collectAsStateWithLifecycle()
    val isEditingMode by viewModel.isEditing.collectAsStateWithLifecycle()

    val formattedToday = getFormattedDateIndo(viewModel.getCurrentDateString())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isEditingMode) Icons.Default.EditCalendar else Icons.Default.AddBusiness,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isEditingMode) "Edit Perintah Kerja" else "Pencatatan OPL Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Bekerja di unit DSO Depok - SA: $saName",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Automatic Date Indicator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QueryBuilder,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tanggal Pencatatan: $formattedToday (Hari Ini)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Fields Block
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cabang Input (Default: DSO Depok)
            OutlinedTextField(
                value = cabang,
                onValueChange = { viewModel.formCabang.value = it },
                label = { Text("Nama Cabang Bengkel") },
                placeholder = { Text("Contoh: DSO Depok") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // SA Name Input (Default: Bunyamin)
            OutlinedTextField(
                value = saName,
                onValueChange = { viewModel.formSaName.value = it },
                label = { Text("Nama Service Advisor (SA)") },
                placeholder = { Text("Contoh: Bunyamin") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // License Plate Input
            OutlinedTextField(
                value = platNomor,
                onValueChange = { viewModel.formPlatNomor.value = it },
                label = { Text("Plat Nomor Kendaraan (Wajib)") },
                placeholder = { Text("Contoh: B 1234 ABC") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Work Types Chips Selection (Multi-select)
            Text(
                text = "Pilih Type Pekerjaan (Bisa pilih lebih dari satu)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Elegant wrap-flow for selection
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.availableJobs.forEach { job ->
                    val isSelected = selectedJobs.contains(job)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onJobSelected(job, !isSelected) },
                        label = { Text(job, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Catatan Text field
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.formNotes.value = it },
                label = { Text("Catatan / Keterangan Tambahan (Opsional)") },
                placeholder = { Text("Masukkan rincian tambahan, keluhan pelanggan atau detail pengerjaan...") },
                minLines = 3,
                maxLines = 5,
                leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save & Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isEditingMode) {
                OutButton(
                    text = "Batal",
                    onClick = { viewModel.cancelEditing() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                OutButton(
                    text = "Reset",
                    onClick = { viewModel.resetForm() },
                    modifier = Modifier.weight(1f)
                )
            }

            FillButton(
                text = if (isEditingMode) "Simpan Perubahan" else "Simpan OPL",
                icon = if (isEditingMode) Icons.Default.Save else Icons.Default.Add,
                onClick = { viewModel.submitOplForm() },
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

// ==========================================
// SCREEN: OPL EDITOR (TOOLS UNTUK EDIT/HAPUS DATA OPL)
// ==========================================
@Composable
fun OplEditorScreen(viewModel: OplViewModel) {
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var entryToDelete by remember { mutableStateOf<OplEntry?>(null) }

    val filteredEntries = remember(allEntries, searchQuery) {
        if (searchQuery.isBlank()) {
            allEntries
        } else {
            allEntries.filter { 
                it.platNomor.contains(searchQuery, ignoreCase = true) ||
                it.saName.contains(searchQuery, ignoreCase = true) ||
                it.tipePekerjaan.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Headings
        Column {
            Text(
                text = "OPL Editor Tools",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Gunakan fitur ini untuk memperbaiki atau menghapus data jika terjadi kesalahan pencatatan.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Search Bar with outline styling
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari Plat Nomor, SA, atau Pekerjaan") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        if (filteredEntries.isEmpty()) {
            EmptyStateContainer(
                message = if (allEntries.isEmpty()) "Belum ada data OPL sama sekali." else "Tidak ada data OPL yang cocok.",
                actionText = "Tampilkan Semua",
                onAction = { searchQuery = "" }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    OplEntryItemCard(
                        entry = entry,
                        onEdit = { viewModel.startEditing(entry, sourceTab = 2) }, // Tab 2 is OPL Editor
                        onDelete = { entryToDelete = entry }
                    )
                }
            }
        }
    }

    // Secure Confirmation Deletion Dialog Representation
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = "Hapus Data OPL?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus data OPL dengan plat nomor ${entryToDelete?.platNomor}? Data ini akan dihapus secara permanen.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete?.let { viewModel.deleteEntry(it) }
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { entryToDelete = null }
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// ==========================================
// SCREEN 3: RIWAYAT OPL (OUTPUT / HISTORY)
// ==========================================
@Composable
fun RiwayatOplScreen(viewModel: OplViewModel) {
    val entries by viewModel.filteredHistoryEntries.collectAsStateWithLifecycle()
    val searchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.historyFilterType.collectAsStateWithLifecycle()
    val customFilterDate by viewModel.selectedFilterDate.collectAsStateWithLifecycle()
    val customFilterStartDate by viewModel.selectedFilterStartDate.collectAsStateWithLifecycle()
    val customFilterEndDate by viewModel.selectedFilterEndDate.collectAsStateWithLifecycle()
    
    val selectedJobFilter by viewModel.historyFilterJob.collectAsStateWithLifecycle()
    val selectedSaFilter by viewModel.historyFilterSa.collectAsStateWithLifecycle()
    val availableSAs by viewModel.availableSAs.collectAsStateWithLifecycle()
    
    val jobStats by viewModel.jobStatistics.collectAsStateWithLifecycle()
    val trendStats by viewModel.trendStatistics.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var currentSubMode by remember { mutableStateOf("list") } // "list" or "charts"
    var showAdvancedFilters by remember { mutableStateOf(false) }
    val hasActiveExpandedFilter = selectedJobFilter != null || selectedSaFilter != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switch (Segmented Control tab) - Sleek interface style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "list" to Pair("Daftar OPL", Icons.Default.List),
                "charts" to Pair("Visualisasi & Grafik", Icons.Default.BarChart)
            ).forEach { (mode, pair) ->
                val (label, icon) = pair
                val isSelected = currentSubMode == mode
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { currentSubMode = mode }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                }
            }
        }

        if (currentSubMode == "charts") {
            // ==========================================
            // CHARTS & VISUAL ANALYTICS MODE
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "TOTAL WORKSHOP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${entries.size} Unit",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Unit masuk dlm filter",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "TIPE PEKERJAAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${jobStats.size} Varian",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Jenis service aktif",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Chart 1: Job Types Distribution (Horizontal Bar Chart)
                Card(
                     modifier = Modifier.fillMaxWidth(),
                     shape = RoundedCornerShape(24.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFEFF6FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Distribusi Pekerjaan OPL",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Jumlah order per tipe pekerjaan",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        if (jobStats.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada transaksi pencatatan OPL.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val maxCount = jobStats.values.maxOrNull() ?: 1
                            
                            // Render list of horizontal bar items
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                jobStats.forEach { (jobName, count) ->
                                    val progress = count.toFloat() / maxCount
                                    val colors = getWorkTagColor(jobName)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(colors.second, CircleShape)
                                                )
                                                Text(
                                                    text = jobName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = "$count Car(s)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = colors.second
                                            )
                                        }

                                        // Bar progress track
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color(0xFFF1F5F9))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = progress)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(
                                                                colors.second.copy(alpha = 0.5f),
                                                                colors.second
                                                            )
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chart 2: Daily Productivity Trend (Vertical Columns Timeline)
                Card(
                     modifier = Modifier.fillMaxWidth(),
                     shape = RoundedCornerShape(24.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Tren Produktivitas Harian",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Grafik aktivitas per tanggal transaksi",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        if (trendStats.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak memiliki tren yang terekam.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val maxTrendVal = trendStats.maxOf { it.second }.toFloat().coerceAtLeast(1f)
                            
                            // Horizontal scroll grid representing chronological flow of vertical column items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                trendStats.forEach { (rawDate, valCount) ->
                                    val heightPercentage = valCount / maxTrendVal
                                    
                                    // Parse to short readable date like "25/05"
                                    val shortDate = try {
                                        val parts = rawDate.split("-")
                                        if (parts.size >= 3) "${parts[2]}/${parts[1]}" else rawDate
                                    } catch (e: Exception) {
                                        rawDate
                                    }
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Bar Tooltip Value
                                        Text(
                                            text = "$valCount Key",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )

                                        // Colum structure
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .fillMaxHeight(fraction = heightPercentage.coerceIn(0.12f, 1f))
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                        )
                                                    )
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Short date label below
                                        Text(
                                            text = shortDate,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // TRANSACTIONS LIST MODE (WITH ADVANCED FILTERS)
            // ==========================================
            // Search & advanced filters triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.historySearchQuery.value = it },
                    placeholder = { Text("Plat Nomor (misal: B 1120)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.historySearchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                // Advanced filters expand button
                IconButton(
                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (hasActiveExpandedFilter) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9),
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Lanjutan",
                            tint = if (hasActiveExpandedFilter) MaterialTheme.colorScheme.primary else Color.DarkGray
                        )
                        // Tiny notification dot on button indicating active advanced filters
                        if (hasActiveExpandedFilter) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // Period Selection Section
            Text(
                text = "Periode Transaksi",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                letterSpacing = 0.8.sp
            )

            // Dynamic filter period row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    HistoryFilter.ALL to "Semua",
                    HistoryFilter.DAILY to "Per Hari",
                    HistoryFilter.WEEKLY to "Minggu",
                    HistoryFilter.MONTHLY to "Bulan",
                    HistoryFilter.RANGE to "Range"
                ).forEach { (type, label) ->
                    val isSelected = activeFilter == type
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.historyFilterType.value = type
                            if (type == HistoryFilter.DAILY) {
                                // Automatically open datepicker for daily
                                val calendar = Calendar.getInstance()
                                viewModel.parseDateString(customFilterDate)?.let { d ->
                                    calendar.time = d
                                }
                                
                                try {
                                    android.app.DatePickerDialog(
                                        context.findActivity() ?: context,
                                        { _, year, month, dayOfMonth ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            }
                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            viewModel.selectedFilterDate.value = sdf.format(newCal.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else if (type == HistoryFilter.RANGE) {
                                // Automatically open start date picker
                                val calendar = Calendar.getInstance()
                                viewModel.parseDateString(customFilterStartDate)?.let { d ->
                                    calendar.time = d
                                }
                                try {
                                    android.app.DatePickerDialog(
                                        context.findActivity() ?: context,
                                        { _, year, month, dayOfMonth ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            }
                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            viewModel.selectedFilterStartDate.value = sdf.format(newCal.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            // Expandable dynamic interactive Datepicker sub-card when Per Hari selected
            AnimatedVisibility(visible = activeFilter == HistoryFilter.DAILY) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable {
                            val calendar = Calendar.getInstance()
                            viewModel.parseDateString(customFilterDate)?.let { d ->
                                calendar.time = d
                            }
                            try {
                                android.app.DatePickerDialog(
                                    context.findActivity() ?: context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        viewModel.selectedFilterDate.value = sdf.format(newCal.time)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Tanggal Terpilih: ${getFormattedDateIndo(customFilterDate)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "GANTI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expandable dynamic active Date Range selector when Range selected
            AnimatedVisibility(visible = activeFilter == HistoryFilter.RANGE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pilih Rentang Tanggal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start Date Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable {
                                    val calendar = Calendar.getInstance()
                                    viewModel.parseDateString(customFilterStartDate)?.let { d ->
                                        calendar.time = d
                                    }
                                    try {
                                        android.app.DatePickerDialog(
                                            context.findActivity() ?: context,
                                            { _, year, month, dayOfMonth ->
                                                val newCal = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                }
                                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                viewModel.selectedFilterStartDate.value = sdf.format(newCal.time)
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text("Mulai", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(getFormattedDateIndo(customFilterStartDate), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }

                        // End Date Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable {
                                    val calendar = Calendar.getInstance()
                                    viewModel.parseDateString(customFilterEndDate)?.let { d ->
                                        calendar.time = d
                                    }
                                    try {
                                        android.app.DatePickerDialog(
                                            context.findActivity() ?: context,
                                            { _, year, month, dayOfMonth ->
                                                val newCal = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                }
                                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                viewModel.selectedFilterEndDate.value = sdf.format(newCal.time)
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text("Selesai", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(getFormattedDateIndo(customFilterEndDate), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Expandable advanced filtering panel (SA & Job type selection)
            AnimatedVisibility(visible = showAdvancedFilters) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Advanced Filters Header title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILTER LANJUTAN AKTIF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            if (hasActiveExpandedFilter) {
                                Text(
                                    text = "Reset Lanjutan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable {
                                        viewModel.historyFilterJob.value = null
                                        viewModel.historyFilterSa.value = null
                                    }
                                )
                            }
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        // Filter 1: Service Advisor select
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Pilih Service Advisor (SA):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Default option
                                FilterChip(
                                    selected = selectedSaFilter == null,
                                    onClick = { viewModel.historyFilterSa.value = null },
                                    label = { Text("Semua SA", fontSize = 11.sp) }
                                )

                                availableSAs.forEach { sa ->
                                    FilterChip(
                                        selected = selectedSaFilter == sa,
                                        onClick = { viewModel.historyFilterSa.value = sa },
                                        label = { Text(sa, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Filter 2: OPL Job Type select
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Filter Sesuai Tipe Pekerjaan:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedJobFilter == null,
                                    onClick = { viewModel.historyFilterJob.value = null },
                                    label = { Text("Semua Kerja", fontSize = 11.sp) }
                                )

                                viewModel.availableJobs.forEach { job ->
                                    FilterChip(
                                        selected = selectedJobFilter == job,
                                        onClick = { viewModel.historyFilterJob.value = job },
                                        label = { Text(job, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Header for result status & overall reset 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hasil Pencarian (${entries.size} Unit)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                
                val isAnyFilterApplied = activeFilter != HistoryFilter.ALL || searchQuery.isNotEmpty() || hasActiveExpandedFilter
                if (isAnyFilterApplied) {
                    Text(
                        text = "Reset Semua Filter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable {
                            viewModel.clearAllHistoryFilters()
                        }
                    )
                }
            }

            // Transactions layout renderer
            if (entries.isEmpty()) {
                EmptyStateContainer(
                    message = "Tidak ada transaksi workshop OPL yang cocok.",
                    actionText = "Bersihkan Filter",
                    onAction = {
                        viewModel.clearAllHistoryFilters()
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { entry ->
                        OplEntryItemCard(
                            entry = entry,
                            onEdit = { viewModel.startEditing(entry, sourceTab = 3) },
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// CARD COMPONENT DECORATORS
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OplEntryItemCard(
    entry: OplEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // First Row: Plat Nomor Badge & Edit/Delete Action Controllers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // License Plate styling resembling Indonesian black/steel plate
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFFF1F5F9).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = entry.platNomor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // Edit and Delete buttons on the right formatted inside a sleek pill container
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit OPL",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus OPL",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Advisor details and date info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Advisor: ${entry.saName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                // Date label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        tint = Color.Gray,
                        contentDescription = "Saat Ini",
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = getFormattedDateIndo(entry.tanggalString),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Work Types Labels Flow
            Text(
                text = "PEKERJAAN OPL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Convert comma-separated string to single badges
            val jobs = entry.tipePekerjaan.split(", ").filter { it.isNotEmpty() }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                jobs.forEach { job ->
                    val colors = getWorkTagColor(job)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.first)
                            .border(1.dp, colors.second.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Sleek indicator dot matching HTML spec status dot style
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(colors.second, CircleShape)
                            )
                            Text(
                                text = job,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.second
                            )
                        }
                    }
                }
            }

            // Optional custom notes
            if (entry.catatan.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Catatan Tambahan:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                        Text(
                            text = entry.catatan,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// Helper to provide colorful tag elements dynamically to make UI stunning
@Composable
fun getWorkTagColor(job: String): Pair<Color, Color> {
    return when (job) {
        "Salon Engine" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)       // Cyan combination
        "Salon Kaca" -> Color(0xFFF0FDF4) to Color(0xFF15803D)         // Green/glass glass
        "Headlamp Treatment" -> Color(0xFFFEF3C7) to Color(0xFFB45309) // Amber bright
        "Salon Interior" -> Color(0xFFECFDF5) to Color(0xFF047857)     // Emerald soft
        "Salon Eksterior" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)    // Purple luxurious
        "Karpet" -> Color(0xFFFCE7F3) to Color(0xFFBE185D)             // Pink
        "EX Banjir" -> Color(0xFFFFEDD5) to Color(0xFFC2410C)          // Orange hazard
        "Rematching Disc" -> Color(0xFFF1F5F9) to Color(0xFF475569)    // Metallic Slate Slate
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
}

// ==========================================
// GENERIC SUB-COMPONENTS
// ==========================================
@Composable
fun EmptyStateContainer(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun FillButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OutButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Helper date localizer
fun getFormattedDateIndo(dateStr: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateStr) ?: return dateStr
        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        return outputFormat.format(date)
    } catch (e: Exception) {
        return dateStr
    }
}
