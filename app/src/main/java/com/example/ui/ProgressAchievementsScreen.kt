package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLanguage
import com.example.data.local.AchievementId
import com.example.data.local.DailyPointsSummary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.QuizCorrectGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressAchievementsModal(
    uiState: QubeeUiState,
    onDismiss: () -> Unit
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val progress = uiState.userProgress
    val goalProgress = (progress.todayPoints.toFloat() / uiState.dailyGoalPoints.coerceAtLeast(1)).coerceIn(0f, 1f)
    val studyMinutes = (progress.totalLearningSeconds / 60L).coerceAtLeast(if (progress.totalPoints > 0) 1 else 0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("progress_achievements_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            text = if (isOm) "Guddina & Badhaasa" else "Progress & Achievements",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close progress")
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top KPI Summary
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Filled.Star, null, tint = GoldAccent, modifier = Modifier.size(28.dp))
                                    Column {
                                        Text(
                                            text = "${progress.totalPoints} XP",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.testTag("total_xp_text")
                                        )
                                        Text(
                                            text = if (isOm) "Qabxii Fayyadamaa (User Points)" else "Total User Points",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.LocalFireDepartment, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "${progress.currentStreakDays}d",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isOm) "Galma Har'aa" else "Today's XP Goal",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "${progress.todayPoints} / ${uiState.dailyGoalPoints} XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { goalProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp)
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(
                                    icon = Icons.Filled.Timer,
                                    label = "${studyMinutes}m",
                                    sub = if (isOm) "Yeroo" else "Study Time"
                                )
                                StatPill(
                                    icon = Icons.Filled.EmojiEvents,
                                    label = "${uiState.quizProgress.accuracyPercent}%",
                                    sub = if (isOm) "Qormaata" else "Quiz Acc."
                                )
                                StatPill(
                                    icon = Icons.Filled.LocalFireDepartment,
                                    label = "${progress.bestStreakDays}d",
                                    sub = if (isOm) "Rikoordii" else "Best Streak"
                                )
                            }
                        }
                    }
                }

                // Weekly XP Bar Chart
                item {
                    PointsHistoryChartCard(
                        title = if (isOm) "Sochii Torbanii (Weekly XP)" else "Weekly Activity (XP)",
                        data = uiState.weeklyPoints,
                        isOm = isOm
                    )
                }

                // Monthly XP Bar Chart
                item {
                    PointsHistoryChartCard(
                        title = if (isOm) "Sochii Ji'aa (Monthly XP)" else "Monthly Activity (XP)",
                        data = uiState.monthlyPoints,
                        isOm = isOm
                    )
                }

                // Achievements Section
                item {
                    Text(
                        text = if (isOm) {
                            "Badhaasawwan (${uiState.unlockedAchievementIds.size}/${AchievementId.entries.size})"
                        } else {
                            "Achievements (${uiState.unlockedAchievementIds.size}/${AchievementId.entries.size})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(AchievementId.entries) { achievement ->
                    val unlocked = achievement.id in uiState.unlockedAchievementIds
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) {
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (unlocked) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                                contentDescription = null,
                                tint = if (unlocked) QuizCorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOm) achievement.titleOm else achievement.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isOm) achievement.descOm else achievement.descEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Recent Quiz History
                if (uiState.quizAttempts.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isOm) "Seenaa Qormaataa" else "Quiz History",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(uiState.quizAttempts.take(8)) { attempt ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(attempt.mode, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${attempt.score}/${attempt.totalQuestions} correct",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "+${attempt.pointsEarned} XP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = QuizCorrectGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sub: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PointsHistoryChartCard(
    title: String,
    data: List<DailyPointsSummary>,
    isOm: Boolean
) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            if (data.isEmpty()) {
                Text(
                    text = if (isOm) "Qormaata ykn barumsa xumuriitii chaartii kana argi." else "Complete lessons or quizzes to populate your activity chart.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxTotal = (data.maxOfOrNull { it.total } ?: 1).coerceAtLeast(1)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val count = data.size.coerceAtLeast(1)
                    val slotWidth = size.width / count
                    val barW = (slotWidth * 0.55f).coerceAtLeast(8f)
                    data.forEachIndexed { i, day ->
                        val ratio = (day.total.toFloat() / maxTotal.toFloat()).coerceIn(0.08f, 1f)
                        val barH = size.height * ratio
                        val left = i * slotWidth + (slotWidth - barW) / 2f
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(left, 0f),
                            size = Size(barW, size.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, size.height - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(data.first().dateKey.takeLast(5), style = MaterialTheme.typography.labelSmall)
                    Text("Max: $maxTotal XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(data.last().dateKey.takeLast(5), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
