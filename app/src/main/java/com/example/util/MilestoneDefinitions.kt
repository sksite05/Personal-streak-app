package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TierBronze
import com.example.ui.theme.TierDiamond
import com.example.ui.theme.TierGold
import com.example.ui.theme.TierPlatinum
import com.example.ui.theme.TierSilver

enum class BadgeTier(val label: String, val color: Color) {
    BRONZE("Bronze", TierBronze),
    SILVER("Silver", TierSilver),
    GOLD("Gold", TierGold),
    PLATINUM("Platinum", TierPlatinum),
    DIAMOND("Diamond", TierDiamond)
}

data class Milestone(
    val days: Int,
    val title: String,
    val description: String,
    val tier: BadgeTier,
    val iconEmoji: String,
    val bonusFreezeEarned: Boolean = true
)

object MilestoneDefinitions {
    val ALL_MILESTONES = listOf(
        Milestone(3, "First Spark", "Ignited your journey with 3 straight days", BadgeTier.BRONZE, "✨", bonusFreezeEarned = false),
        Milestone(7, "Weekly Warrior", "One full week of solid discipline", BadgeTier.BRONZE, "🔥", bonusFreezeEarned = true),
        Milestone(14, "Fortnight Titan", "Two unstoppable weeks in the zone", BadgeTier.SILVER, "⚡", bonusFreezeEarned = false),
        Milestone(21, "Habit Architect", "Neuroplasticity locked in (21 days)", BadgeTier.SILVER, "🧠", bonusFreezeEarned = true),
        Milestone(30, "Monthly Master", "Conquered an entire month with grace", BadgeTier.GOLD, "🏆", bonusFreezeEarned = true),
        Milestone(50, "Golden Dynamo", "50 straight days of pure excellence", BadgeTier.GOLD, "🌟", bonusFreezeEarned = true),
        Milestone(75, "Diamond Vanguard", "75 days of unwavering resilience", BadgeTier.PLATINUM, "💎", bonusFreezeEarned = true),
        Milestone(100, "Centurion", "Triple digits! You belong in the hall of fame", BadgeTier.PLATINUM, "👑", bonusFreezeEarned = true),
        Milestone(150, "Iron Titan", "Unshakable will and relentless focus", BadgeTier.PLATINUM, "🛡️", bonusFreezeEarned = true),
        Milestone(200, "Apex Sovereign", "200 days of legendary mastery", BadgeTier.DIAMOND, "🪐", bonusFreezeEarned = true),
        Milestone(365, "Mythic Legend", "A complete year of daily victory", BadgeTier.DIAMOND, "🌌", bonusFreezeEarned = true)
    )

    fun getNextMilestone(currentStreak: Int): Milestone? {
        return ALL_MILESTONES.firstOrNull { it.days > currentStreak }
    }

    fun getUnlockedMilestones(longestStreak: Int): List<Milestone> {
        return ALL_MILESTONES.filter { it.days <= longestStreak }
    }

    fun calculateLevel(totalXp: Int): Pair<Int, String> {
        return when {
            totalXp >= 2500 -> 5 to "Legend"
            totalXp >= 1200 -> 4 to "Unstoppable"
            totalXp >= 600 -> 3 to "Disciplined"
            totalXp >= 200 -> 2 to "Consistent"
            else -> 1 to "Beginner"
        }
    }

    fun getLevelThresholds(level: Int): Pair<Int, Int> {
        return when (level) {
            1 -> 0 to 200
            2 -> 200 to 600
            3 -> 600 to 1200
            4 -> 1200 to 2500
            else -> 2500 to 5000
        }
    }
}
