package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
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
    onDismiss: () -> Unit,
    onSelectTextScale: (Float) -> Unit = {},
    onSelectRepeatCount: (Int) -> Unit = {},
    onSelectDailyGoal: (Int) -> Unit = {},
    onToggleReminders: (Boolean) -> Unit = {},
    onSignInGoogle: (Context) -> Unit = {},
    onSyncCloud: () -> Unit = {},
    onSignOutCloud: () -> Unit = {},
    onResetAllData: () -> Unit = {}
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val context = LocalContext.current
    val totalSaved = uiState.favoriteProverbIds.size + uiState.favoritePhraseIds.size + uiState.favoriteVocabIds.size
    var showResetConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onToggleReminders(granted)
    }

    fun handleRemindersChanged(enabled: Boolean) {
        if (!enabled) {
            onToggleReminders(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) onToggleReminders(true) else notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onToggleReminders(true)
        }
    }

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
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close settings")
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
                    SettingsSectionCard(title = if (isOm) "Afaan Appichaa" else "App Language") {
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

                // Theme Appearance & Text Size Accessibility
                item {
                    SettingsSectionCard(title = if (isOm) "Bifa & Guddina Barreeffamaa" else "Appearance & Text Size") {
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
                        Text(
                            text = if (isOm) "Guddina Qubee: ${(uiState.textScale * 100).toInt()}%" else "Reading Text Scale: ${(uiState.textScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = uiState.textScale,
                            onValueChange = onSelectTextScale,
                            valueRange = 0.85f..1.30f,
                            steps = 8,
                            modifier = Modifier.testTag("text_scale_slider")
                        )
                    }
                }

                // Speech Speed & Repeat Controls
                item {
                    SettingsSectionCard(title = if (isOm) "Saffisa fi Irra-Deebii Sagalee" else "Audio Speed & Repeat") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp).align(Alignment.CenterVertically)
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
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 2, 3).forEach { repeat ->
                                FilterChip(
                                    selected = uiState.audioState.repeatCount == repeat,
                                    onClick = { onSelectRepeatCount(repeat) },
                                    label = { Text(if (isOm) "Irra deebii ${repeat}x" else "Repeat ${repeat}x") }
                                )
                            }
                        }
                    }
                }

                // Daily Goal & Reminders
                item {
                    SettingsSectionCard(title = if (isOm) "Galma Guyyaa & Yaadachiisa" else "Daily Goal & Reminders") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(30, 50, 100, 150).forEach { goal ->
                                FilterChip(
                                    selected = uiState.dailyGoalPoints == goal,
                                    onClick = { onSelectDailyGoal(goal) },
                                    label = { Text("$goal XP") }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = if (isOm) "Yaadachiisa Guyyaa (7:00 PM)" else "Daily Study Reminder (7:00 PM)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (isOm) "Walitti fufiinsa kee eeguuf" else "Keep your daily learning streak alive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.remindersEnabled,
                                onCheckedChange = ::handleRemindersChanged,
                                modifier = Modifier.testTag("reminders_switch")
                            )
                        }
                    }
                }

                // Optional Cloud Backup (Google Sign-In + Firestore)
                item {
                    SettingsSectionCard(title = if (isOm) "Kuusaa Duumessaa (Firebase Cloud)" else "Firebase & Cloud Backup (Google)") {
                        val cloud = uiState.cloudSyncState
                        Surface(
                            color = if (cloud.isFirebaseReady) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("firebase_connection_badge")
                        ) {
                            Text(
                                text = if (cloud.isFirebaseReady) {
                                    val pid = cloud.firebaseProjectId?.let { " ($it)" } ?: ""
                                    if (isOm) "Firebase walqabateera • Firestore & AI Qophii$pid" else "Firebase Connected • Firestore & AI Logic Ready$pid"
                                } else {
                                    if (isOm) "Firebase yeroo ammaa hin argamne" else "Firebase offline/uninitialized in this runtime"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (cloud.isFirebaseReady) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        if (!cloud.isSignedIn) {
                            Text(
                                text = if (isOm) {
                                    "Filannoo: Google'n seenii qabxii fi filatamoo kee duumessarratti olkaa'i."
                                } else {
                                    "Optional: Sign in with Google to sync your XP, streaks, and favorites across devices."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onSignInGoogle(context) },
                                enabled = !cloud.isSyncing,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_signin_button")
                            ) {
                                if (cloud.isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isOm) "Google'n Seeni" else "Sign in with Google")
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.CloudDone, null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cloud.displayName ?: "Aki Learner", style = MaterialTheme.typography.titleSmall)
                                    Text(cloud.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = onSyncCloud,
                                    enabled = !cloud.isSyncing,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Filled.Sync, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isOm) "Amma Sync Godhi" else "Sync Now")
                                }
                                OutlinedButton(
                                    onClick = onSignOutCloud,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(if (isOm) "Ba'i" else "Sign Out")
                                }
                            }
                        }
                        cloud.statusMessage?.let { msg ->
                            Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Daily Quiz, Bookmarks & Reset Management
                item {
                    SettingsSectionCard(title = if (isOm) "Qormaata Guyyaa & Kuusaa" else "Quiz, Library & Data") {
                        Text(
                            text = if (isOm) {
                                "Mammaaksa ${uiState.allProverbs.size} • Haasawa ${uiState.allPhrases.size} • Jechoota ${uiState.allVocabulary.size} • Qubee ${OromoContent.qubeeLetters.size}"
                            } else {
                                "${uiState.allProverbs.size} Proverbs • ${uiState.allPhrases.size} Phrases • ${uiState.allVocabulary.size} Vocab • ${OromoContent.qubeeLetters.size} Qubee"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOm) {
                                "XP: ${uiState.userProgress.totalPoints} • Qormaata: ${uiState.quizProgress.accuracyPercent}% (${uiState.quizProgress.totalCorrect}/${uiState.quizProgress.totalAnswered})"
                            } else {
                                "User Points: ${uiState.userProgress.totalPoints} XP • Quiz: ${uiState.quizProgress.accuracyPercent}% (${uiState.quizProgress.totalCorrect}/${uiState.quizProgress.totalAnswered})"
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
                            Icon(Icons.Filled.Quiz, null, modifier = Modifier.size(18.dp))
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
                                Icon(Icons.Filled.DeleteOutline, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isOm) "Filatamoo Hunda Haqi ($totalSaved)" else "Clear All Favorites ($totalSaved)")
                            }
                        }
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_progress_button")
                        ) {
                            Text(if (isOm) "Odeeffannoo Barumsaa Haqi (Reset)" else "Reset Learning Progress")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showAboutDialog = true }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Info, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isOm) "Waa'ee Appii" else "About Aki Qubee")
                            }
                            TextButton(onClick = { showPrivacyDialog = true }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.PrivacyTip, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isOm) "Iccitii" else "Privacy Policy")
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
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
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
                                    modifier = Modifier.size(52.dp).clip(CircleShape)
                                )
                                Column {
                                    Text(
                                        text = if (isOm) "Odeeffannoo Qopheessaa" else "Developer Information",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Aki Qubee • Powered by AKI TECH • v${BuildConfig.VERSION_NAME}",
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

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(if (isOm) "Odeeffannoo Hunda Haquu?" else "Reset Learning Progress?") },
            text = {
                Text(
                    if (isOm) {
                        "Kuni XP, walitti fufiinsa guyyaa, seenaa qormaataa fi filatamoo meeshaa kanarra jiran hunda haqa."
                    } else {
                        "This clears local User Points, streaks, quiz history, and bookmarks on this device."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirm = false
                    }
                ) {
                    Text(if (isOm) "Haqi" else "Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(if (isOm) "Dhiisi" else "Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Aki Qubee • AKI TECH") },
            text = {
                Text(
                    "\"Learn Afaan Oromoo. Speak with Confidence.\"\n\n" +
                        "Aki Qubee is an offline-first bilingual Afaan Oromoo & English learning platform with 33 Qubee letters, " +
                        "1,270+ vocabulary words, 1,310+ daily conversations, 1,200+ Mammaaksa proverbs, 8-mode quizzes, " +
                        "and AI pronunciation feedback."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(if (isOm) "Iccitii Fayyadamaa" else "Privacy Policy") },
            text = {
                Text(
                    "• Vocabulary, proverbs, phrases, points, and quiz history are stored locally on your device.\n" +
                        "• Voice recordings are used strictly on your request to replay locally or send to Firebase AI Logic for pronunciation coaching.\n" +
                        "• Cloud Backup via Google Sign-In is completely optional and only syncs when signed in."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun DeveloperDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueTag: String? = null
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp).size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}
