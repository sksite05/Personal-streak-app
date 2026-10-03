package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitLog
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.FreezeIceBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HeatmapCalendar(
    logs: List<HabitLog>,
    modifier: Modifier = Modifier,
    onDayClick: ((String, Boolean) -> Unit)? = null
) {
    val completedDates = remember(logs) {
        logs.associateBy { it.dateString }
    }

    val cal = Calendar.getInstance()
    val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
    val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Prepare calendar grid for current month
    val daysInMonth = remember {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        val month = c.get(Calendar.MONTH)
        val firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<CalendarDayInfo?>()
        // Padding for first week (Sunday = 1, adjust to Monday start: (firstDayOfWeek + 5) % 7)
        val leadingBlanks = (firstDayOfWeek + 5) % 7
        for (i in 0 until leadingBlanks) {
            days.add(null)
        }

        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (d in 1..maxDays) {
            c.set(Calendar.DAY_OF_MONTH, d)
            val dateStr = df.format(c.time)
            days.add(CalendarDayInfo(dayNumber = d, dateString = dateStr))
        }
        days
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Heatmap Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Missed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(FlameAmber)
                    )
                    Text("Done", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of week labels
            val weekDayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (h in weekDayHeaders) {
                    Text(
                        text = h,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Grid rows
            val rows = daysInMonth.chunked(7)
            for (week in rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0 until 7) {
                        val day = week.getOrNull(i)
                        if (day == null) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val log = completedDates[day.dateString]
                            val isCompleted = log != null
                            val isFreeze = log?.isFreezeUsed == true
                            val isToday = day.dateString == todayString

                            val cellColor = when {
                                isFreeze -> FreezeIceBlue
                                isCompleted -> FlameAmber
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellColor)
                                    .then(
                                        if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                        else if (!isCompleted) Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        onDayClick?.invoke(day.dateString, isCompleted)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayNumber.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isCompleted || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CalendarDayInfo(
    val dayNumber: Int,
    val dateString: String
)
