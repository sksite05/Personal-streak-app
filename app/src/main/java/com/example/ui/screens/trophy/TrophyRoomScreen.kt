package com.example.ui.screens.trophy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CustomReward
import com.example.ui.MainViewModel
import com.example.util.Milestone
import com.example.util.MilestoneDefinitions
import com.example.util.StreakCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrophyRoomScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val mainHabit by viewModel.mainHabit.collectAsStateWithLifecycle()
    val habitLogs by viewModel.habitLogs.collectAsStateWithLifecycle()
    val customRewards by viewModel.customRewards.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    val streakResult = remember(habitLogs, userSettings.streakFreezesCount) {
        StreakCalculator.calculateStreaks(habitLogs, userSettings.streakFreezesCount > 0)
    }

    val (currentLevel, levelTitle) = MilestoneDefinitions.calculateLevel(userSettings.totalXp)

    var showAddRewardDialog by remember { mutableStateOf(false) }
    var rewardTitleInput by remember { mutableStateOf("") }
    var rewardDaysInput by remember { mutableStateOf("7") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trophy Room & Rewards",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("trophy_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRewardDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_custom_reward_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Promised Reward")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Level & XP Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.trophy_streaksip_1791011823258),
                            contentDescription = "Trophy Banner",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "LEVEL $currentLevel • $levelTitle",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${userSettings.totalXp} Total XP",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Streak record: ${streakResult.longestStreak} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Real-Life Promised Rewards Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 Custom Promised Rewards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showAddRewardDialog = true }) {
                        Text("+ Add Reward")
                    }
                }
            }

            if (customRewards.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Promise yourself real rewards (e.g. 'Watch a movie at 7 days') to boost your motivation!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(customRewards) { reward ->
                    val isEligible = streakResult.longestStreak >= reward.targetDays
                    CustomRewardCard(
                        reward = reward,
                        isEligible = isEligible,
                        currentStreak = streakResult.currentStreak,
                        onClaim = { viewModel.claimCustomReward(reward.id) },
                        onDelete = { viewModel.deleteCustomReward(reward.id) }
                    )
                }
            }

            // Milestone Badges Section (Bronze, Silver, Gold, Platinum, Diamond)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏆 Milestone Badges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(MilestoneDefinitions.ALL_MILESTONES) { milestone ->
                val isUnlocked = streakResult.longestStreak >= milestone.days
                val progress = (streakResult.longestStreak.toFloat() / milestone.days).coerceIn(0f, 1f)

                MilestoneBadgeCard(
                    milestone = milestone,
                    isUnlocked = isUnlocked,
                    progress = progress,
                    currentStreak = streakResult.longestStreak
                )
            }
        }
    }

    // Add Custom Reward Dialog
    if (showAddRewardDialog) {
        AlertDialog(
            onDismissRequest = { showAddRewardDialog = false },
            title = { Text("Promise Yourself a Reward", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("What will you treat yourself to when you reach a milestone?", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rewardTitleInput,
                        onValueChange = { rewardTitleInput = it },
                        placeholder = { Text("e.g. Buy new running shoes, sushi feast") },
                        label = { Text("Reward Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rewardDaysInput,
                        onValueChange = { rewardDaysInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Streak Days Target (e.g. 7, 14, 30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = rewardDaysInput.toIntOrNull() ?: 7
                        if (rewardTitleInput.isNotBlank()) {
                            viewModel.addCustomReward(days, rewardTitleInput)
                            rewardTitleInput = ""
                            showAddRewardDialog = false
                        }
                    }
                ) {
                    Text("Save Reward")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRewardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CustomRewardCard(
    reward: CustomReward,
    isEligible: Boolean,
    currentStreak: Int,
    onClaim: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isClaimed) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (reward.isClaimed) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (reward.isClaimed) Icons.Default.Check else Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = if (reward.isClaimed) Color(0xFF16A34A) else Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${reward.targetDays}-Day Milestone",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (reward.isClaimed) "Claimed! 🥳" else if (isEligible) "Milestone reached! Ready to claim." else "${reward.targetDays - currentStreak} days left",
                    fontSize = 12.sp,
                    color = if (reward.isClaimed) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!reward.isClaimed && isEligible) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Claim")
                }
            } else if (!reward.isClaimed) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun MilestoneBadgeCard(
    milestone: Milestone,
    isUnlocked: Boolean,
    progress: Float,
    currentStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge emblem or locked progress ring
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(milestone.tier.color.copy(alpha = 0.2f))
                            .border(2.dp, milestone.tier.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = milestone.iconEmoji, fontSize = 24.sp)
                    }
                } else {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(52.dp),
                        color = milestone.tier.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 3.5.dp
                    )
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${milestone.days} DAYS • ${milestone.tier.label.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) milestone.tier.color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "UNLOCKED",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    } else {
                        Text(
                            text = "$currentStreak / ${milestone.days} days",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
