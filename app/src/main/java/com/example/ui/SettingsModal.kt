package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.R
import com.example.data.AppLanguage
import com.example.data.OromoContent

private val developerSkills = listOf(
    "Java & Java Swing",
    "Python",
    "PHP & MySQL",
    "HTML, CSS & JavaScript",
    "Flutter & Dart",
    "Android Development",
    "Database Management",
    "UI/UX Design",
    "Information Systems Development"
)

private val developerInterests = listOf(
    "Software Development",
    "Mobile Application Development",
    "Web Development",
    "Information Systems",
    "Database Systems",
    "AI & Emerging Technologies"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsModal(
    uiState: QubeeUiState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeOverride: (Boolean?) -> Unit,
    onSelectTtsSpeed: (Float) -> Unit,
    onClearFavorites: () -> Unit,
    onOpenQuiz: () -> Unit,
    onDismiss: () -> Unit
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val totalSaved = uiState.favoriteProverbIds.size + uiState.favoritePhraseIds.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    title = {
                        Text(
                            text = if (isOm) "Qindaa'ina & Odeeffannoo" else "Settings & Developer Info",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("close_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = if (isOm) "Cufi" else "Close settings"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("settings_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Language Setting
                item {
                    SettingsSectionCard(
                        title = if (isOm) "Afaan Appichaa" else "App Language"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.language == AppLanguage.OROMO,
                                onClick = { onSelectLanguage(AppLanguage.OROMO) },
                                label = { Text("Afaan Oromoo (OM)") },
                                modifier = Modifier.testTag("settings_lang_om")
                            )
                            FilterChip(
                                selected = uiState.language == AppLanguage.ENGLISH,
                                onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                                label = { Text("English (EN)") },
                                modifier = Modifier.testTag("settings_lang_en")
                            )
                        }
                    }
                }

                // Theme Appearance Setting
                item {
                    SettingsSectionCard(
                        title = if (isOm) "Bifa Fuulaa (Theme)" else "Appearance"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.darkThemeOverride == null,
                                onClick = { onSelectThemeOverride(null) },
                                label = { Text(if (isOm) "Sirna" else "System") }
                            )
                            FilterChip(
                                selected = uiState.darkThemeOverride == false,
                                onClick = { onSelectThemeOverride(false) },
                                label = { Text(if (isOm) "Ifaa" else "Light") }
                            )
                            FilterChip(
                                selected = uiState.darkThemeOverride == true,
                                onClick = { onSelectThemeOverride(true) },
                                label = { Text(if (isOm) "Dukkana" else "Dark") }
                            )
                        }
                    }
                }

                // Speech Pronunciation Speed
                item {
                    SettingsSectionCard(
                        title = if (isOm) "Saffisa Sagalee (TTS)" else "Pronunciation Speed (TTS)"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            FilterChip(
                                selected = uiState.ttsSpeed <= 0.75f,
                                onClick = { onSelectTtsSpeed(0.72f) },
                                label = { Text(if (isOm) "Suuta" else "Slow") },
                                modifier = Modifier.testTag("tts_speed_slow")
                            )
                            FilterChip(
                                selected = uiState.ttsSpeed in 0.76f..0.95f,
                                onClick = { onSelectTtsSpeed(0.88f) },
                                label = { Text(if (isOm) "Giddugaleessa" else "Normal") },
                                modifier = Modifier.testTag("tts_speed_normal")
                            )
                            FilterChip(
                                selected = uiState.ttsSpeed > 0.95f,
                                onClick = { onSelectTtsSpeed(1.05f) },
                                label = { Text(if (isOm) "Saffisaa" else "Crisp") },
                                modifier = Modifier.testTag("tts_speed_fast")
                            )
                        }
                    }
                }

                // Daily Quiz & Bookmarks Management
                item {
                    SettingsSectionCard(
                        title = if (isOm) "Qormaata Guyyaa & Kuusaa" else "Daily Quiz & Library"
                    ) {
                        Text(
                            text = if (isOm) {
                                "Mammaaksa ${uiState.allProverbs.size} • Haasawa ${uiState.allPhrases.size} • Qubee ${OromoContent.qubeeLetters.size} • Filatamoo $totalSaved"
                            } else {
                                "${uiState.allProverbs.size} Proverbs • ${uiState.allPhrases.size} Phrases • ${OromoContent.qubeeLetters.size} Qubee • $totalSaved Saved"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOm) {
                                "Milkaa'ina Qormaataa: ${uiState.quizProgress.accuracyPercent}% (${uiState.quizProgress.totalCorrect}/${uiState.quizProgress.totalAnswered}) • Rikoordii: ${uiState.quizProgress.bestStreak}"
                            } else {
                                "Quiz Progress: ${uiState.quizProgress.accuracyPercent}% (${uiState.quizProgress.totalCorrect}/${uiState.quizProgress.totalAnswered}) • Best streak: ${uiState.quizProgress.bestStreak}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenQuiz()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_open_quiz_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isOm) "Qormaata Guyyaa Eegali" else "Open Daily Quiz")
                        }
                        if (totalSaved > 0) {
                            OutlinedButton(
                                onClick = onClearFavorites,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("clear_favorites_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isOm) "Filatamoo Hunda Haqi ($totalSaved)" else "Clear All Favorites ($totalSaved)")
                            }
                        }
                    }
                }

                // Complete Developer Information Card
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("developer_info_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.aki_logo),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                )
                                Column {
                                    Text(
                                        text = if (isOm) "Odeeffannoo Qopheessaa" else "Developer Information",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Aki Qubee • v${BuildConfig.VERSION_NAME}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                            DeveloperDetailRow(
                                icon = Icons.Filled.Code,
                                label = if (isOm) "Maqaa Qopheessaa / Developer Name" else "Developer Name",
                                value = "Aklilu Tibebu",
                                valueTag = "developer_name_text"
                            )
                            DeveloperDetailRow(
                                icon = Icons.Filled.WorkOutline,
                                label = if (isOm) "Ogummaa / Profession" else "Profession",
                                value = "BSc Information Systems Student & Software Developer"
                            )
                            DeveloperDetailRow(
                                icon = Icons.Filled.School,
                                label = if (isOm) "Yunivarsiitii & Muummee" else "University & Department",
                                value = "Madda Walabu University • Department of Information Systems (4th Year)"
                            )
                            DeveloperDetailRow(
                                icon = Icons.Filled.LocationOn,
                                label = if (isOm) "Bakka & Xiyyeeffannoo" else "Location & Specialization",
                                value = "Ethiopia • Information Systems & Software Development"
                            )
                            DeveloperDetailRow(
                                icon = Icons.Filled.Email,
                                label = if (isOm) "Poortifooliyoo & Quunnamtii" else "Portfolio & Contact",
                                value = "Aklilu Tibebu / Aki Tech • akliluasebot@gmail.com"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isOm) "Dandeettiiwwan (Skills)" else "Skills",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    developerSkills.forEach { skill ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = skill,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isOm) "Fedhiiwwan (Interests)" else "Interests",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    developerInterests.forEach { interest ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = interest,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueTag: String? = null
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = if (valueTag != null) Modifier.testTag(valueTag) else Modifier
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}
