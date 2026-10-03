package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.HeatmapCalendar
import com.example.ui.components.StreakFlame
import com.example.ui.components.TopGreetingBar
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.FlameGold
import com.example.util.MilestoneDefinitions
import com.example.util.StreakCalculator

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToWater: () -> Unit,
    onNavigateToTrophy: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val mainHabit by viewModel.mainHabit.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val habitLogs by viewModel.habitLogs.collectAsStateWithLifecycle()
    val waterLogsToday by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val allWaterLogs by viewModel.allWaterLogs.collectAsStateWithLifecycle()
    val customRewards by viewModel.customRewards.collectAsStateWithLifecycle()
    val celebrationMilestone by viewModel.celebrationMilestone.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()

    val streakResult = remember(habitLogs, userSettings.streakFreezesCount, mainHabit) {
        StreakCalculator.calculateStreaks(habitLogs, userSettings.streakFreezesCount > 0)
    }

    val currentStreakDisplay = maxOf(mainHabit?.currentStreak ?: 0, streakResult.currentStreak)
    val longestStreakDisplay = maxOf(mainHabit?.longestStreak ?: 0, streakResult.longestStreak)
    val totalCompletionsDisplay = maxOf(mainHabit?.totalCompletions ?: 0, streakResult.totalCompletions)
    val isDone = streakResult.isTodayCompleted || habitLogs.any { it.dateString == StreakCalculator.getTodayString() }

    val waterProgress = remember(waterLogsToday, allWaterLogs, userSettings) {
        viewModel.getWaterProgressInfo()
    }

    val nextMilestone = remember(currentStreakDisplay) {
        MilestoneDefinitions.getNextMilestone(currentStreakDisplay)
    }

    val nextCustomReward = remember(customRewards, currentStreakDisplay) {
        customRewards.filter { !it.isClaimed && it.targetDays > currentStreakDisplay }
            .minByOrNull { it.targetDays }
    }

    val motivationalQuotes = remember {
        listOf(
            "“We are what we repeatedly do. Excellence, then, is not an act, but a habit.”",
            "“Drink your water! Your brain is 75% water—stay sharp and energized.”",
            "“Small daily improvements over time lead to stunning results.”",
            "“One sip, one habit, one milestone at a time.”",
            "“Consistency is the DNA of mastery.”"
        )
    }
    var quoteIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Top Bar: Greeting + Level & XP + Streak Freezes + Trophy Room Link
            TopGreetingBar(
                totalXp = userSettings.totalXp,
                streakFreezes = userSettings.streakFreezesCount,
                onTrophyClick = onNavigateToTrophy,
                onSettingsClick = onNavigateToSettings,
                onFreezeClick = {
                    if (streakResult.streakAtRisk && userSettings.streakFreezesCount > 0) {
                        viewModel.useFreeze()
                    }
                }
            )

            // Daily Motivational Tip / Quote
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { quoteIndex = (quoteIndex + 1) % motivationalQuotes.size },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = motivationalQuotes[quoteIndex],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Streak At Risk Alert Banner
            if (streakResult.streakAtRisk) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("streak_at_risk_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF2F2)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Streak at risk!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Mark today as done to keep your ${streakResult.currentStreak}-day momentum alive.",
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp
                            )
                        }
                        if (userSettings.streakFreezesCount > 0 && !streakResult.isYesterdayCompleted) {
                            Button(
                                onClick = { viewModel.useFreeze() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Use 🧊", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // HABIT STREAK HERO CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("habit_streak_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Habit Title & Switcher Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mainHabit?.emoji ?: "⚡",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mainHabit?.name ?: "Daily Habit",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onNavigateToHabits() }
                        ) {
                            Text(
                                text = "Habits ➔",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Flame & Streak Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StreakFlame(
                            streakDays = currentStreakDisplay,
                            size = 80.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$currentStreakDisplay",
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (currentStreakDisplay > 0) FlameAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = " DAYS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                            }
                            Text(
                                text = if (isDone) "Streak active & blazing! 🔥" else "Ready to conquer today!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Satisfying "Mark as Done Today" Action Button
                    val buttonColor by animateColorAsState(
                        targetValue = if (isDone) Color(0xFF10B981) else FlameAmber,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "buttonColor"
                    )

                    Button(
                        onClick = {
                            if (isDone) {
                                viewModel.unmarkTodayDone()
                            } else {
                                viewModel.markTodayDone()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("mark_done_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        AnimatedVisibility(visible = isDone) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "COMPLETED TODAY!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                            }
                        }

                        AnimatedVisibility(visible = !isDone) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔥 MARK AS DONE TODAY",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Secondary action: Mark yesterday as done (within 24 hrs) or undo
                    val isYesterdayDone = streakResult.isYesterdayCompleted || habitLogs.any { it.dateString == StreakCalculator.getYesterdayString() }
                    if (!isYesterdayDone) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { viewModel.markYesterdayDone() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("mark_yesterday_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Yesterday as Done (Late Log)", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats summary: Longest Streak, Total Completions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$longestStreakDisplay",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Longest Streak",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalCompletionsDisplay",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total Done",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // WATER TRACKER COMPACT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToWater() }
                    .testTag("water_card_preview"),
                shape = RoundedCornerShape(24.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalDrink,
                                    contentDescription = "Water Bottle",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Daily Hydration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Bottle ${waterProgress.bottlesFinishedCount + 1} • ${waterProgress.currentIntakeTodayMl} / ${waterProgress.dailyGoalMl} ml",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open Water Tracker",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val waterFraction = (waterProgress.currentIntakeTodayMl.toFloat() / waterProgress.dailyGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { waterFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF0284C7),
                        trackColor = Color(0xFFE0F2FE)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status nudge
                        val diff = waterProgress.differenceMl
                        val statusText = when {
                            waterProgress.isGoalReached -> "🎉 Daily Goal Met! Great job!"
                            diff < 0 -> "Behind by ${-diff} ml • Drink up!"
                            else -> "On track! Hydration hero! 💧"
                        }
                        val statusColor = if (diff < 0 && !waterProgress.isGoalReached) Color(0xFFD97706) else Color(0xFF0284C7)

                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )

                        // Quick Add +250ml
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier
                                .clickable { viewModel.addWater(250) }
                                .testTag("quick_add_250_home")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0284C7))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+250 ml", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }
            }

            // NEXT REWARD / MILESTONE PROGRESS BAR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToTrophy() }
                    .testTag("next_reward_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    val targetDays = nextMilestone?.days ?: 365
                    val remaining = (targetDays - streakResult.currentStreak).coerceAtLeast(0)
                    val prevDays = if (nextMilestone != null) {
                        val idx = MilestoneDefinitions.ALL_MILESTONES.indexOf(nextMilestone)
                        if (idx > 0) MilestoneDefinitions.ALL_MILESTONES[idx - 1].days else 0
                    } else 0
                    val span = (targetDays - prevDays).coerceAtLeast(1)
                    val progress = ((streakResult.currentStreak - prevDays).toFloat() / span).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NEXT MILESTONE REWARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${nextMilestone?.title ?: "Legend"} (${targetDays} Days)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$remaining days to go",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FlameGold,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    if (nextCustomReward != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🎁 Promised Reward: ${nextCustomReward.title}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            // GITHUB-STYLE MONTH CALENDAR HEATMAP
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completion Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onNavigateToStats) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "View full stats", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                HeatmapCalendar(
                    logs = habitLogs,
                    onDayClick = { dateStr, done ->
                        // Interactive tap
                    }
                )
            }
        }

        // Particle Confetti overlay
        ConfettiEffect(
            trigger = confettiTrigger,
            onFinished = { viewModel.resetConfetti() }
        )

        // Milestone Unlocked Celebration Dialog
        CelebrationDialog(
            milestone = celebrationMilestone,
            promisedRewardTitle = nextCustomReward?.title,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }
}
