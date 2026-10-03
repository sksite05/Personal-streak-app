package com.example.ui.screens.water

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.WaterBottleCanvas
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val waterLogsToday by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val allWaterLogs by viewModel.allWaterLogs.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()

    val progressInfo = remember(waterLogsToday, allWaterLogs, userSettings) {
        viewModel.getWaterProgressInfo()
    }
    val scheduleSlots = remember(userSettings) {
        viewModel.getWaterScheduleSlots()
    }

    var showCustomAmountDialog by remember { mutableStateOf(false) }
    var customAmountInput by remember { mutableStateOf("") }
    var showScheduleEditDialog by remember { mutableStateOf(false) }
    var customSlotsCsvInput by remember { mutableStateOf(userSettings.customScheduleCsv) }

    // Check if refilled bottle animation should show
    val isBottleFull = progressInfo.currentBottleMl >= userSettings.bottleCapacityMl && progressInfo.currentIntakeTodayMl > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Water Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Streak: ${progressInfo.currentWaterStreakDays} Days 🔥",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("water_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            customSlotsCsvInput = userSettings.customScheduleCsv
                            showScheduleEditDialog = true
                        },
                        modifier = Modifier.testTag("edit_water_schedule_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Schedule")
                    }
                    IconButton(
                        onClick = {
                            if (progressInfo.differenceMl < 0) {
                                NotificationHelper.showWaterNudgeNotification(context, -progressInfo.differenceMl)
                            }
                        }
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Test Notification")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Stats summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S INTAKE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${progressInfo.currentIntakeTodayMl}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / ${progressInfo.dailyGoalMl} ml",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                )
                            }
                        }

                        // Bottles finished pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Bottle ${progressInfo.bottlesFinishedCount + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0369A1)
                                )
                                Text(
                                    text = "Goal: ${progressInfo.dailyGoalMl / userSettings.bottleCapacityMl} bottles",
                                    fontSize = 10.sp,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Schedule Nudge / Status Message
                val diff = progressInfo.differenceMl
                val (nudgeBg, nudgeColor, nudgeIcon, nudgeMessage) = when {
                    progressInfo.isGoalReached -> Quad(
                        Color(0xFFECFDF5),
                        Color(0xFF047857),
                        Icons.Default.CheckCircle,
                        "Daily Goal Reached! You are exceptionally well-hydrated! 🎉"
                    )
                    diff < 0 -> Quad(
                        Color(0xFFFFFBEB),
                        Color(0xFFB45309),
                        Icons.Default.Info,
                        "You're ${-diff} ml behind schedule. Take a fresh glass now!"
                    )
                    else -> Quad(
                        Color(0xFFF0FDF4),
                        Color(0xFF15803D),
                        Icons.Default.CheckCircle,
                        "You're ${diff} ml ahead of schedule! Hydration Hero!"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = nudgeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = nudgeIcon, contentDescription = null, tint = nudgeColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = nudgeMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = nudgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 1000ml Canvas Water Bottle
                Text(
                    text = "Tap or drag on the bottle to adjust water level directly",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                WaterBottleCanvas(
                    currentBottleMl = progressInfo.currentBottleMl,
                    bottleCapacityMl = userSettings.bottleCapacityMl,
                    targetLevelNowMl = progressInfo.targetLevelNowMl,
                    scheduleSlots = scheduleSlots,
                    height = 360.dp,
                    interactive = true,
                    onWaterLevelChanged = { newMl ->
                        viewModel.setBottleWaterLevel(newMl)
                    },
                    modifier = Modifier.testTag("interactive_water_bottle")
                )

                // Bottle Refill Alert / Multi-bottle counter
                if (progressInfo.bottlesFinishedCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0284C7))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bottle #${progressInfo.bottlesFinishedCount} completed & refilled! 🔄",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // QUICK ADD BUTTONS (+100ml, +200ml, +250ml, Custom)
                Text(
                    text = "QUICK ADD WATER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickAmounts = listOf(100, 200, 250)
                    for (amount in quickAmounts) {
                        Button(
                            onClick = { viewModel.addWater(amount) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_add_${amount}_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("+$amount ml", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Custom amount button
                    Button(
                        onClick = { showCustomAmountDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("custom_water_amount_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            "Custom",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Undo last entry button
                if (waterLogsToday.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.undoLastSip() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("undo_water_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val lastAmount = waterLogsToday.firstOrNull()?.amountMl ?: 0
                        Text("Undo Last Entry ($lastAmount ml)", fontSize = 13.sp)
                    }
                }
            }

            // Confetti overlay
            ConfettiEffect(
                trigger = confettiTrigger,
                onFinished = { viewModel.resetConfetti() }
            )
        }
    }

    // Custom Amount Dialog
    if (showCustomAmountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAmountDialog = false },
            title = { Text("Log Custom Amount", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter water amount in milliliters (ml):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customAmountInput,
                        onValueChange = { customAmountInput = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g. 350") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = customAmountInput.toIntOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.addWater(amount)
                        }
                        customAmountInput = ""
                        showCustomAmountDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAmountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Schedule Customization Dialog
    if (showScheduleEditDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleEditDialog = false },
            title = { Text("Water Schedule Times", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Set target time labels for 250ml, 500ml, 750ml, and 1000ml markings (comma-separated):",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customSlotsCsvInput,
                        onValueChange = { customSlotsCsvInput = it },
                        placeholder = { Text("08:00, 11:00, 14:00, 17:00") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSettings(
                            userSettings.copy(customScheduleCsv = customSlotsCsvInput)
                        )
                        showScheduleEditDialog = false
                    }
                ) {
                    Text("Save Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
