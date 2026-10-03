package com.example.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HabitLog
import com.example.data.model.WaterLog
import com.example.ui.MainViewModel
import com.example.ui.components.HeatmapCalendar
import com.example.ui.theme.FlameAmber
import com.example.util.StreakCalculator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val habitLogs = viewModel.habitLogs.collectAsStateWithLifecycle().value
    val allWaterLogs = viewModel.allWaterLogs.collectAsStateWithLifecycle().value
    val userSettings = viewModel.userSettings.collectAsStateWithLifecycle().value

    val streakResult = remember(habitLogs, userSettings.streakFreezesCount) {
        StreakCalculator.calculateStreaks(habitLogs, userSettings.streakFreezesCount > 0)
    }

    // Weekly Water Intake Data for the past 7 days
    val past7DaysData = remember(allWaterLogs, userSettings.dailyWaterGoalMl) {
        val list = mutableListOf<DayWaterStat>()
        val cal = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayLabelDf = SimpleDateFormat("EEE", Locale.US)

        val waterMap = allWaterLogs.groupBy { it.dateString }

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = df.format(c.time)
            val label = dayLabelDf.format(c.time)
            val amount = waterMap[dateStr]?.sumOf { it.amountMl } ?: 0
            list.add(DayWaterStat(label = label, dateString = dateStr, amountMl = amount))
        }
        list
    }

    // Completion rate this month
    val completionRate = remember(habitLogs) {
        val cal = Calendar.getInstance()
        val daysInMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthStr = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val completedThisMonth = habitLogs.count { it.dateString.startsWith(monthStr) }
        val rate = (completedThisMonth.toFloat() / daysInMonth.coerceAtLeast(1) * 100).toInt()
        rate.coerceIn(0, 100)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("stats_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key Metrics Summary Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Current Streak",
                        value = "${streakResult.currentStreak} Days",
                        subtitle = "Active Flame",
                        icon = Icons.Default.Whatshot,
                        iconTint = FlameAmber,
                        containerColor = Color(0xFFFFF7ED)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Completion Rate",
                        value = "$completionRate%",
                        subtitle = "This Month",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF10B981),
                        containerColor = Color(0xFFECFDF5)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Longest Streak",
                        value = "${streakResult.longestStreak} Days",
                        subtitle = "Personal Record",
                        icon = Icons.Default.Whatshot,
                        iconTint = Color(0xFFF59E0B),
                        containerColor = Color(0xFFFEF3C7)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Sessions",
                        value = "${streakResult.totalCompletions}",
                        subtitle = "Check-ins logged",
                        icon = Icons.Default.LocalDrink,
                        iconTint = Color(0xFF0284C7),
                        containerColor = Color(0xFFE0F2FE)
                    )
                }
            }

            // Water Intake 7-Day Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Water Intake (Last 7 Days)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Daily Goal: ${userSettings.dailyWaterGoalMl} ml",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "7 Days",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Bar Chart
                        val goal = userSettings.dailyWaterGoalMl.coerceAtLeast(1)
                        val maxDisplay = maxOf(goal, past7DaysData.maxOfOrNull { it.amountMl } ?: goal)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            for (day in past7DaysData) {
                                val fraction = (day.amountMl.toFloat() / maxDisplay).coerceIn(0.04f, 1f)
                                val isGoalMet = day.amountMl >= goal

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "${day.amountMl}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGoalMet) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((110 * fraction).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(if (isGoalMet) Color(0xFF0284C7) else Color(0xFF93C5FD))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Month Heatmap Calendar
            item {
                Text(
                    text = "Habit Consistency Heatmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                HeatmapCalendar(logs = habitLogs)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

data class DayWaterStat(
    val label: String,
    val dateString: String,
    val amountMl: Int
)
