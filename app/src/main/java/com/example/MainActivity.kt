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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextStyle
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
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3500)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

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
                    subtitle = "Depok Service Office (DSO)"
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
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedEnigmaHeader()
        }
    }
}

@Composable
fun AnimatedEnigmaHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "enigma_header")

    val text = "ENIGMA" // Matching the requested text
    val textColor = MaterialTheme.colorScheme.onBackground
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(4.dp)
    ) {
        text.forEachIndexed { index, char ->
            if (char == ' ') {
                Spacer(modifier = Modifier.width(8.dp))
                return@forEachIndexed
            }
            // staggered delay calculation
            val delay = index * 105 // 0.105s steps based on css
            
            val floatAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 4000 // 4s linear loop
                        0f at 0
                        1f at 200 // 5% mark
                        0.2f at 800 // 20% mark
                        0f at 4000 // 100% mark
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(delay)
                ),
                label = "letter_anim_$index"
            )

            Text(
                text = char.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = textColor.copy(alpha = floatAnim.coerceIn(0f, 1f)),
                        blurRadius = (floatAnim * 12f).coerceAtLeast(0.01f)
                    )
                ),
                color = textColor.copy(alpha = floatAnim.coerceIn(0.4f, 1f)), // Minimum opacity 0.4 so it's not totally invisible
                modifier = Modifier.graphicsLayer {
                    val scale = 1f + (floatAnim * 0.1f)
                    scaleX = scale
                    scaleY = scale
                    translationY = -floatAnim * 4f
                }
            )
        }
    }
}

@Composable
fun Animated3DBottomNavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    activeColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate when pressed or selected
    val isActive = isPressed || selected

    val animationProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f, // spring-like cubic-bezier feel
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "click_anim"
    )

    // Child (Container) transforms
    val containerRotationX = animationProgress * 60f
    val containerTranslationY = animationProgress * 8f 
    
    // Button (Icon) transforms 
    // translate3d(0, 20, 30) rotateX(-60deg) 
    val iconRotationX = animationProgress * -60f
    val iconTranslationY = animationProgress * -15f // pop out upwards

    Box(
        modifier = modifier
            .height(64.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple for custom 3D effect
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Container
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(48.dp)
                .graphicsLayer {
                    rotationX = containerRotationX
                    cameraDistance = 12f
                    translationY = containerTranslationY.dp.toPx()
                }
                .background(
                    color = if (isActive) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isActive) 1.dp else 0.dp,
                    color = if (isActive) activeColor.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icon (Button) popping out
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationX = iconRotationX
                    cameraDistance = 12f
                    translationY = iconTranslationY.dp.toPx()
                }
            ) {
                icon()
            }
        }
        
        // Hide Text when active, show when inactive
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .graphicsLayer {
                    alpha = 1f - animationProgress
                    translationY = (animationProgress * 10f).dp.toPx()
                }
        )
    }
}

@Composable
fun OplBottomNavBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Animated3DBottomNavBarItem(
                selected = activeTab == 0,
                onClick = { onTabSelected(0) },
                label = "Daily OPL",
                icon = {
                    Icon(
                        imageVector = if (activeTab == 0) Icons.Default.Today else Icons.Outlined.Today,
                        contentDescription = "Daily OPL",
                        tint = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Animated3DBottomNavBarItem(
                selected = activeTab == 1,
                onClick = { onTabSelected(1) },
                label = "OPL Create",
                icon = {
                    Icon(
                        imageVector = if (activeTab == 1) Icons.Default.AddBox else Icons.Outlined.AddBox,
                        contentDescription = "OPL Create",
                        tint = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Animated3DBottomNavBarItem(
                selected = activeTab == 2,
                onClick = { onTabSelected(2) },
                label = "OPL Editor",
                icon = {
                    Icon(
                        imageVector = if (activeTab == 2) Icons.Default.Edit else Icons.Outlined.Edit,
                        contentDescription = "Editor",
                        tint = if (activeTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Animated3DBottomNavBarItem(
                selected = activeTab == 3,
                onClick = { onTabSelected(3) },
                label = "Data OPL",
                icon = {
                    Icon(
                        imageVector = if (activeTab == 3) Icons.Default.History else Icons.Outlined.History,
                        contentDescription = "Data OPL",
                        tint = if (activeTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
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

    val vehicleType by viewModel.formVehicleType.collectAsStateWithLifecycle()
    val vehicleOther by viewModel.formVehicleOther.collectAsStateWithLifecycle()
    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedJob by remember { mutableStateOf(false) }

    val formDate by viewModel.formDate.collectAsStateWithLifecycle()
    val isToday = formDate == viewModel.getCurrentDateString()
    val formattedDate = getFormattedDateIndo(formDate)

    val context = LocalContext.current

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
            modifier = Modifier.fillMaxWidth().clickable {
                val calendar = Calendar.getInstance()
                viewModel.parseDateString(formDate)?.let { d ->
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
                            viewModel.formDate.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(newCal.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
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
                Column {
                    Text(
                        text = "Tanggal Pencatatan: $formattedDate " + if(isToday) "(Hari Ini)" else "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ketuk untuk mengubah tanggal",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
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

            // Vehicle Type Dropdown
            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expandedVehicle,
                onExpandedChange = { expandedVehicle = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    value = vehicleType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type Kendaraan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicle) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedVehicle,
                    onDismissRequest = { expandedVehicle = false }
                ) {
                    viewModel.availableVehicles.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.formVehicleType.value = selectionOption
                                expandedVehicle = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            if (vehicleType == "Others") {
                OutlinedTextField(
                    value = vehicleOther,
                    onValueChange = { viewModel.formVehicleOther.value = it },
                    label = { Text("Tipe Kendaraan Lainnya") },
                    placeholder = { Text("Masukkan manual...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Work Types Dropdown Selection
            val selectedJobsDisplay = selectedJobs.joinToString(", ")
            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expandedJob,
                onExpandedChange = { expandedJob = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    value = selectedJobsDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipe Pekerjaan (Bisa pilih ganda)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJob) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedJob,
                    onDismissRequest = { expandedJob = false }
                ) {
                    viewModel.availableJobs.forEach { selectionOption ->
                        val isSelected = selectedJobs.contains(selectionOption)
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isSelected, onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectionOption)
                                }
                            },
                            onClick = {
                                viewModel.onJobSelected(selectionOption, !isSelected)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
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
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    if (isEditing) {
        PencatatanOplScreen(viewModel)
        return
    }

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
        CyberSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Cari Plat Nomor, SA, atau Pekerjaan",
            modifier = Modifier.fillMaxWidth()
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
    val chartMonth by viewModel.chartSelectedMonth.collectAsStateWithLifecycle()
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    val targetJobPrices by viewModel.targetJobPrices.collectAsStateWithLifecycle()
    
    var expandedEntryId by remember { mutableStateOf<Int?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showDummyDeleteConfirm by remember { mutableStateOf(false) }
    var showExportFilterDialog by remember { mutableStateOf(false) }
    var selectedExportJobs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedJobForPrice by remember { mutableStateOf("Salon Mesin") }
    
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
                "list" to Pair("Daftar", Icons.Default.List),
                "charts" to Pair("Grafik", Icons.Default.BarChart),
                "delete" to Pair("Hapus Data", Icons.Default.DeleteSweep)
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (currentSubMode == "charts") {
            // ==========================================
            // CHARTS & VISUAL ANALYTICS MODE
            // ==========================================
            val availableMonths = remember(allEntries) {
                allEntries.map { it.tanggalString.take(7) }.distinct().sortedDescending()
            }
            var expandedMonth by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Month Picker & Export PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = expandedMonth,
                        onExpandedChange = { expandedMonth = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            value = chartMonth,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Bulan (Berdasarkan Data)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMonth,
                            onDismissRequest = { expandedMonth = false }
                        ) {
                            if (availableMonths.isEmpty()) {
                                DropdownMenuItem(text = { Text("Belum ada data") }, onClick = { expandedMonth = false })
                            }
                            availableMonths.forEach { monthOption ->
                                DropdownMenuItem(
                                    text = { Text(monthOption) },
                                    onClick = {
                                        viewModel.chartSelectedMonth.value = monthOption
                                        expandedMonth = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                    
                    // Button Export Grafik PDF
                    IconButton(
                        onClick = { 
                            com.example.util.PdfExporter.exportGrafikPdf(
                                context = context,
                                month = chartMonth,
                                jobStats = jobStats,
                                trendStats = trendStats
                            )
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Summary Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalUnit = trendStats.sumOf { it.second }
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
                                text = "$totalUnit Unit",
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

                // Target & Insentif Module
                val totalJobsCount = jobStats.values.sum()
                var totalRevenue = 0L
                jobStats.forEach { (job, count) ->
                    val price = (targetJobPrices[job] ?: "120000").toLongOrNull() ?: 120000L
                    totalRevenue += (count * price)
                }
                
                var incentivePercent = 0
                if (totalRevenue > 75_000_000) incentivePercent = 6
                else if (totalRevenue > 55_000_000) incentivePercent = 5
                else if (totalRevenue > 45_000_000) incentivePercent = 4
                else if (totalRevenue > 35_000_000) incentivePercent = 3
                else if (totalRevenue > 25_000_000) incentivePercent = 2
                else if (totalRevenue > 15_000_000) incentivePercent = 1
                
                val totalIncentive = (totalRevenue * incentivePercent) / 100

                val formatRp = remember { java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 } }
                
                Card(
                     modifier = Modifier.fillMaxWidth(),
                     shape = RoundedCornerShape(24.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFF3E8FF), CircleShape), // Purple tint
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = Color(0xFF9333EA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Pendapatan & Insentif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Target berdasarkan nilai total pekerjaan",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    val entriesList: List<com.example.data.OplEntry> = allEntries
                                    val availableJobs = entriesList.filter { it.tanggalString.take(7) == chartMonth }
                                        .flatMap { it.tipePekerjaan.split(",").map { j -> j.trim() }.filter { j -> j.isNotEmpty() } }
                                        .toSet()
                                    selectedExportJobs = availableJobs
                                    showExportFilterDialog = true
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF3E8FF), CircleShape)
                                        .size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "Export PDF",
                                        tint = Color(0xFF9333EA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total Pekerjaan",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$totalJobsCount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = "Atur Harga Pekerjaan",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.weight(1.2f)) {
                                        var expanded by remember { mutableStateOf(false) }
                                        OutlinedButton(
                                            onClick = { expanded = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Text(
                                                text = selectedJobForPrice,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            val combinedJobs = (viewModel.availableJobs + jobStats.keys).distinct().sorted()
                                            combinedJobs.forEach { job ->
                                                DropdownMenuItem(
                                                    text = { Text(job, fontSize = 12.sp) },
                                                    onClick = { 
                                                        selectedJobForPrice = job
                                                        expanded = false 
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    val currentPrice = targetJobPrices[selectedJobForPrice] ?: "120000"
                                    OutlinedTextField(
                                        value = currentPrice,
                                        onValueChange = { viewModel.updateJobPrice(selectedJobForPrice, it) },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color(0xFFE2E8F0)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Total Pendapatan",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formatRp.format(totalRevenue),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Insentif ($incentivePercent%)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formatRp.format(totalIncentive),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF10B981) // Emerald Green for earnings
                                )
                            }
                        }
                        
                        // Progress bar towards next target (Optional visual polish)
                        val nextTarget = when {
                            totalRevenue < 15_000_000 -> Pair(15_000_000L, 1)
                            totalRevenue < 25_000_000 -> Pair(25_000_000L, 2)
                            totalRevenue < 35_000_000 -> Pair(35_000_000L, 3)
                            totalRevenue < 45_000_000 -> Pair(45_000_000L, 4)
                            totalRevenue < 55_000_000 -> Pair(55_000_000L, 5)
                            totalRevenue < 75_000_000 -> Pair(75_000_000L, 6)
                            else -> null
                        }
                        
                        if (nextTarget != null) {
                            val prevTarget = when {
                                totalRevenue >= 55_000_000 -> 55_000_000L
                                totalRevenue >= 45_000_000 -> 45_000_000L
                                totalRevenue >= 35_000_000 -> 35_000_000L
                                totalRevenue >= 25_000_000 -> 25_000_000L
                                totalRevenue >= 15_000_000 -> 15_000_000L
                                else -> 0L
                            }
                            
                            val progress = ((totalRevenue - prevTarget).toFloat() / (nextTarget.first - prevTarget)).coerceIn(0f, 1f)
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Menuju Insentif ${nextTarget.second}%",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "${formatRp.format(totalRevenue)} / ${formatRp.format(nextTarget.first)}",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF9333EA),
                                    trackColor = Color(0xFFF3E8FF)
                                )
                            }
                        } else {
                             Text(
                                 text = "Target Max Tercapai! Luar biasa \uD83C\uDF89",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = Color(0xFF10B981),
                                 modifier = Modifier.fillMaxWidth(),
                                 textAlign = TextAlign.Center
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
                                                text = "$count Unit",
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
                                            text = "$valCount Unit",
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
                CyberSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.historySearchQuery.value = it },
                    placeholder = "Plat Nomor (misal: B 1120)",
                    modifier = Modifier.fillMaxWidth(),
                    onFilterClick = { showAdvancedFilters = !showAdvancedFilters },
                    hasActiveFilter = hasActiveExpandedFilter
                )
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
                            
                            val showSingleDatePicker = {
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

                            if (type == HistoryFilter.DAILY || type == HistoryFilter.WEEKLY || type == HistoryFilter.MONTHLY) {
                                showSingleDatePicker()
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

            // Expandable dynamic interactive Datepicker sub-card when Per Hari, Minggu, Bulan selected
            AnimatedVisibility(visible = activeFilter in listOf(HistoryFilter.DAILY, HistoryFilter.WEEKLY, HistoryFilter.MONTHLY)) {
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
                            text = when (activeFilter) {
                                HistoryFilter.WEEKLY -> {
                                    val start = customFilterDate
                                    val cal = Calendar.getInstance()
                                    viewModel.parseDateString(start)?.let { d -> cal.time = d }
                                    cal.add(Calendar.DAY_OF_YEAR, 6)
                                    val endFull = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                    val endLabel = getFormattedDateIndo(endFull)
                                    "${getFormattedDateIndo(start)} - $endLabel"
                                }
                                HistoryFilter.MONTHLY -> {
                                    viewModel.parseDateString(customFilterDate)?.let { d ->
                                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(d)
                                    } ?: getFormattedDateIndo(customFilterDate)
                                }
                                else -> getFormattedDateIndo(customFilterDate)
                            },
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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

            if (currentSubMode == "delete") {
                Spacer(modifier = Modifier.height(8.dp))
                val dummyEntries = allEntries.filter { it.catatan == "Demo data OPL - Auto-generated" }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (entries.isNotEmpty()) {
                        Button(
                            onClick = { showBulkDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Hapus Terpilih", 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.generateDemoData() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Buat Dummy", 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }

                    if (dummyEntries.isNotEmpty()) {
                        Button(
                            onClick = { showDummyDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Hapus Dummy", 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                if (showBulkDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showBulkDeleteConfirm = false },
                        title = { Text("Konfirmasi Hapus Data") },
                        text = { Text("Apakah Anda yakin ingin menghapus ${entries.size} data OPL yang terpilih? Tindakan ini tidak dapat dibatalkan.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteEntries(entries)
                                    showBulkDeleteConfirm = false
                                    currentSubMode = "list"
                                }
                            ) { Text("Ya, Hapus Semua", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBulkDeleteConfirm = false }) { Text("Batal") }
                        }
                    )
                }

                if (showDummyDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDummyDeleteConfirm = false },
                        title = { Text("Konfirmasi Hapus Data Dummy") },
                        text = { Text("Apakah Anda yakin ingin menghapus seluruh (${dummyEntries.size}) data dummy? Data riil tidak akan terhapus.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteEntries(dummyEntries)
                                    showDummyDeleteConfirm = false
                                }
                            ) { Text("Ya, Hapus Dummy", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDummyDeleteConfirm = false }) { Text("Batal") }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { entry ->
                        if (expandedEntryId == entry.id) {
                            OplEntryItemCard(
                                entry = entry,
                                onEdit = { 
                                    expandedEntryId = null
                                    viewModel.startEditing(entry, sourceTab = 3) 
                                },
                                onDelete = { viewModel.deleteEntry(entry) }
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { expandedEntryId = entry.id }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.platNomor,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = entry.typeKendaraan,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = entry.tanggalString,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportFilterDialog) {
        val entriesList: List<com.example.data.OplEntry> = allEntries
        val availableJobsThisMonth = remember(chartMonth, entriesList) {
            entriesList.filter { it.tanggalString.take(7) == chartMonth }
                .flatMap { it.tipePekerjaan.split(",").map { j -> j.trim() }.filter { j -> j.isNotEmpty() } }
                .distinct()
                .sorted()
        }

        AlertDialog(
            onDismissRequest = { showExportFilterDialog = false },
            title = { Text("Pilih Pekerjaan untuk PDF", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pilih aktivitas yang ingin disertakan dalam Export PDF bulan ini:", fontSize = 14.sp)
                    availableJobsThisMonth.forEach { job ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExportJobs = if (selectedExportJobs.contains(job)) {
                                        selectedExportJobs - job
                                    } else {
                                        selectedExportJobs + job
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = selectedExportJobs.contains(job),
                                onCheckedChange = { isChecked ->
                                    selectedExportJobs = if (isChecked) {
                                        selectedExportJobs + job
                                    } else {
                                        selectedExportJobs - job
                                    }
                                }
                            )
                            Text(text = job, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val filteredEntries = entriesList.filter { entry ->
                            entry.tanggalString.take(7) == chartMonth && 
                            entry.tipePekerjaan.split(",").map { j -> j.trim() }.filter { j -> j.isNotEmpty() }.any { selectedExportJobs.contains(it) }
                        }
                        val filteredTotalRevenue = filteredEntries.sumOf { entry -> 
                            entry.tipePekerjaan.split(",").map { j -> j.trim() }.filter { j -> j.isNotEmpty() }
                                .sumOf { job -> 
                                    if (selectedExportJobs.contains(job)) (targetJobPrices[job] ?: "120000").toLongOrNull() ?: 120000L else 0L 
                                }
                        }
                        val filteredTotalJobsCount = filteredEntries.sumOf { entry ->
                            entry.tipePekerjaan.split(",").map { j -> j.trim() }.filter { j -> j.isNotEmpty() }.count { selectedExportJobs.contains(it) }
                        }
                        
                        var fIncentivePercent = 0
                        if (filteredTotalRevenue > 75_000_000) fIncentivePercent = 6
                        else if (filteredTotalRevenue > 55_000_000) fIncentivePercent = 5
                        else if (filteredTotalRevenue > 45_000_000) fIncentivePercent = 4
                        else if (filteredTotalRevenue > 35_000_000) fIncentivePercent = 3
                        else if (filteredTotalRevenue > 25_000_000) fIncentivePercent = 2
                        else if (filteredTotalRevenue > 15_000_000) fIncentivePercent = 1
                        
                        com.example.util.PdfExporter.exportPendapatanPdf(
                            context = context,
                            month = chartMonth,
                            entries = filteredEntries,
                            totalRevenue = filteredTotalRevenue,
                            totalJobsCount = filteredTotalJobsCount,
                            incentivePercent = fIncentivePercent,
                            targetJobPrices = targetJobPrices,
                            selectedJobs = selectedExportJobs
                        )
                        showExportFilterDialog = false
                    },
                    enabled = selectedExportJobs.isNotEmpty()
                ) {
                    Text("Export PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportFilterDialog = false }) {
                    Text("Batal")
                }
            }
        )
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
            
            if (entry.typeKendaraan.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Kendaraan: ${entry.typeKendaraan}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                        .background(Color.Transparent)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Catatan Tambahan:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = entry.catatan,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
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
        "Salon Mesin" -> Color(0xFFE2E8F0) to Color(0xFF0F172A) // Muted slate bg, dark text
        "Salon Kaca" -> Color(0xFFD1FAE5) to Color(0xFF064E3B) // Muted earthy green bg, dark text
        "Headlamp Treatment" -> Color(0xFFFEF3C7) to Color(0xFF78350F) // Muted warm bg, dark text
        "Salon Interior" -> Color(0xFFCCFBF1) to Color(0xFF134E4A) // Muted cyan bg, dark text
        "Salon Exterior" -> Color(0xFFE9D5FF) to Color(0xFF4C1D95) // Muted purple bg, dark text
        "Karpet" -> Color(0xFFFCE7F3) to Color(0xFF831843) // Muted pink bg, dark text
        "Ex Banjir" -> Color(0xFFFFEDD5) to Color(0xFF7C2D12) // Muted orange bg, dark text
        "Rematching" -> Color(0xFFF1F5F9) to Color(0xFF1E293B) // Muted gray bg, dark text
        "Jasa Engine" -> Color(0xFFE0E7FF) to Color(0xFF312E81) // Muted indigo bg, dark text
        "Quick Wax" -> Color(0xFFFEF08A) to Color(0xFF713F12) // Muted yellow bg, dark text
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun CyberSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null,
    hasActiveFilter: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer_offset"
    )

    // Futuristic container with glow
    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val tertiaryGlow = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Left Glow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterStart)
                .height(56.dp)
                .offset(x = (-10).dp)
                .blur(30.dp)
                .background(primaryGlow, CircleShape)
        )
        // Right Glow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterEnd)
                .height(56.dp)
                .offset(x = 10.dp)
                .blur(30.dp)
                .background(tertiaryGlow, CircleShape)
        )

        // Main Search Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color.Transparent, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                        start = Offset(0f, 0f),
                        end = Offset(100f, 100f)
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            // Skewed shimmer effect (like HTML gradient skewed translating)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val shimmerWidth = 200f
                val skewOffset = 100f // to skew the drawing
                
                // Draw a skewed polygon that moves across
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(gradientOffset, size.height)
                    lineTo(gradientOffset + shimmerWidth, size.height)
                    lineTo(gradientOffset + shimmerWidth + skewOffset, 0f)
                    lineTo(gradientOffset + skewOffset, 0f)
                    close()
                }
                
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.08f), Color.Transparent),
                        start = Offset(gradientOffset, 0f),
                        end = Offset(gradientOffset + shimmerWidth + skewOffset, 0f)
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )

                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (onFilterClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    val filterBg = if (hasActiveFilter) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    val filterBorder = if (hasActiveFilter) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    val filterIconTint = if (hasActiveFilter) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(filterBg)
                            .border(1.dp, filterBorder, RoundedCornerShape(10.dp))
                            .clickable(onClick = onFilterClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = filterIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(180.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loader
            CyberCircuitLoader(
                modifier = Modifier
                    .width(280.dp)
                    .aspectRatio(800f / 500f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Typewriter text
            TypewriterText(
                text = "Sedang Memuat Data . . .",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CyberCircuitLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "circuit")
    val phase by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = CubicBezierEasing(0.5f, 0f, 0.9f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val sx = size.width / 800f
        val sy = size.height / 500f

        val traceBgColor = Color(0xFF333333)
        val strokeW = 1.8f * (sx + sy) / 2f
        
        fun buildPath(startX: Float, startY: Float, midX: Float, midY: Float, endX: Float): androidx.compose.ui.graphics.Path {
            return androidx.compose.ui.graphics.Path().apply {
                moveTo(startX * sx, startY * sy)
                lineTo(midX * sx, startY * sy)
                lineTo(midX * sx, midY * sy)
                lineTo(endX * sx, midY * sy)
            }
        }

        fun drawTrace(path: androidx.compose.ui.graphics.Path, color: Color) {
            drawPath(path, traceBgColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))
            
            val effect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(40f * sx, 400f * sx), phase * sx)
            
            drawPath(
                path, 
                color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeW, 
                    pathEffect = effect
                )
            )
            
            drawPath(
                path, 
                color.copy(alpha = 0.5f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeW * 3f, 
                    pathEffect = effect
                )
            )
        }

        val purple = Color(0xFF9900FF)
        val blue = Color(0xFF00CCFF)
        val yellow = Color(0xFFFFEA00)
        val green = Color(0xFF00FF15)
        val red = Color(0xFFFF3300)

        // Traces
        drawTrace(buildPath(100f, 100f, 200f, 210f, 326f), purple)
        drawTrace(buildPath(80f, 180f, 180f, 230f, 326f), blue)
        drawTrace(buildPath(60f, 260f, 150f, 250f, 326f), yellow)
        drawTrace(buildPath(100f, 350f, 200f, 270f, 326f), green)
        
        drawTrace(buildPath(700f, 90f, 560f, 210f, 474f), blue)
        drawTrace(buildPath(740f, 160f, 580f, 230f, 474f), green)
        drawTrace(buildPath(720f, 250f, 590f, 250f, 474f), red)
        drawTrace(buildPath(680f, 340f, 570f, 270f, 474f), yellow)

        val circleColor = Color.Black
        val r = 5f * sx
        drawCircle(circleColor, r, Offset(100f * sx, 100f * sy))
        drawCircle(circleColor, r, Offset(80f * sx, 180f * sy))
        drawCircle(circleColor, r, Offset(60f * sx, 260f * sy))
        drawCircle(circleColor, r, Offset(100f * sx, 350f * sy))
        
        drawCircle(circleColor, r, Offset(700f * sx, 90f * sy))
        drawCircle(circleColor, r, Offset(740f * sx, 160f * sy))
        drawCircle(circleColor, r, Offset(720f * sx, 250f * sy))
        drawCircle(circleColor, r, Offset(680f * sx, 340f * sy))

        val chipBrush = Brush.verticalGradient(listOf(Color(0xFF2D2D2D), Color(0xFF0F0F0F)))
        drawRoundRect(
            brush = chipBrush,
            topLeft = Offset(330f * sx, 190f * sy),
            size = androidx.compose.ui.geometry.Size(140f * sx, 100f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f * sx, 20f * sy)
        )
        drawRoundRect(
            color = Color(0xFF222222),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f * sx),
            topLeft = Offset(330f * sx, 190f * sy),
            size = androidx.compose.ui.geometry.Size(140f * sx, 100f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f * sx, 20f * sy)
        )

        val pinBrush = Brush.horizontalGradient(listOf(Color(0xFFBBBBBB), Color(0xFF888888), Color(0xFF555555)))
        fun drawPin(pw: Float, ph: Float, px: Float, py: Float) {
            drawRoundRect(
                brush = pinBrush,
                topLeft = Offset(px * sx, py * sy),
                size = androidx.compose.ui.geometry.Size(pw * sx, ph * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy)
            )
        }
        val pinYs = listOf(205f, 225f, 245f, 265f)
        for (py in pinYs) {
            drawPin(8f, 10f, 322f, py)
            drawPin(8f, 10f, 470f, py)
        }
    }
}

@Composable
fun TypewriterText(text: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    var textToDisplay by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        textToDisplay = ""
        for (i in text.indices) {
            textToDisplay += text[i]
            kotlinx.coroutines.delay(80) 
        }
    }
    
    Text(
        text = textToDisplay,
        modifier = modifier,
        color = color,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        fontSize = 18.sp,
        letterSpacing = 2.sp
    )
}
