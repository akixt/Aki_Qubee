package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.audio.AudioPlaybackState
import com.example.data.OromoContent
import com.example.firebase.AiModelState
import com.example.firebase.PronunciationComparisonResult
import com.example.ui.theme.QuizCorrectGreen
import com.example.ui.theme.QuizIncorrectRed

private val speakingPresets = listOf(
    SpeakingTarget("Akkam jirta?", "How are you? (singular)", "ah-kahm jeer-tah"),
    SpeakingTarget("Galatoomi", "Thank you", "gah-lah-toh-mee"),
    SpeakingTarget("Qubee Afaan Oromoo", "Oromo Alphabet", "qoo-bee ah-faan oh-roh-moh"),
    SpeakingTarget("Dhugaa dubbadhu", "Speak the truth", "dhoo-gaa doob-bah-dhoo"),
    SpeakingTarget("Bunni baay'ee mi'aawa", "The coffee is delicious", "boon-nee baay-ee mee-ah-wah")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpeakingPracticeModal(
    isOm: Boolean,
    target: SpeakingTarget,
    audioState: AudioPlaybackState,
    aiComparisonState: AiModelState<PronunciationComparisonResult>,
    aiModelAudioState: AiModelState<Unit>,
    onSelectTarget: (SpeakingTarget) -> Unit,
    onPlayAiModel: () -> Unit,
    onSpeakFallback: (String) -> Unit,
    onStartRecording: () -> Boolean,
    onStopRecording: () -> Boolean,
    onPlayRecording: () -> Unit,
    onCompareWithAi: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            onStartRecording()
        } else {
            permissionDenied = true
        }
    }

    fun handleRecordClick() {
        if (audioState.isRecording) {
            onStopRecording()
        } else {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                permissionDenied = false
                onStartRecording()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
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
                .testTag("speaking_practice_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    title = {
                        Column {
                            Text(
                                text = if (isOm) "Shaakala Dubbii (AI)" else "AI Pronunciation Studio",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isOm) {
                                    "Sagalee waraabiitii moodeela AI waliin madaali"
                                } else {
                                    "Record your voice & compare with AI pronunciation model"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("close_speaking_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close speaking lab")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Presets
                Text(
                    text = if (isOm) "Jechoota Shaakalaa Filadhu" else "Choose Target Expression",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    speakingPresets.forEachIndexed { idx, preset ->
                        FilterChip(
                            selected = target.targetOromo == preset.targetOromo,
                            onClick = { onSelectTarget(preset) },
                            label = { Text(preset.targetOromo) },
                            modifier = Modifier.testTag("speaking_preset_$idx")
                        )
                    }
                }

                // Target Card
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = target.targetOromo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = target.targetEnglish,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (target.phoneticTip.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${if (isOm) "Akkaataa dubbisa" else "Phonetic guide"}: ${target.phoneticTip}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onPlayAiModel,
                                enabled = aiModelAudioState !is AiModelState.Loading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("play_ai_model_button")
                            ) {
                                if (aiModelAudioState is AiModelState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(if (isOm) "Sagalee AI" else "AI Model Audio")
                            }

                            OutlinedButton(
                                onClick = { onSpeakFallback(target.targetOromo) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("play_tts_fallback_button")
                            ) {
                                Text(if (isOm) "TTS" else "Device TTS")
                            }
                        }

                        if (aiModelAudioState is AiModelState.Error) {
                            Text(
                                text = aiModelAudioState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Voice Recording Controls
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isOm) "1. Sagalee Kee Waraabi" else "1. Record Your Pronunciation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = ::handleRecordClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (audioState.isRecording) QuizIncorrectRed else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("record_voice_button")
                        ) {
                            Icon(
                                imageVector = if (audioState.isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (audioState.isRecording) {
                                    if (isOm) "Waraabii Dhaabi" else "Stop Recording"
                                } else {
                                    if (isOm) "Waraabii Eegali" else "Tap to Record"
                                },
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        if (permissionDenied) {
                            Text(
                                text = if (isOm) {
                                    "Hayyama maayikiroofoonii qindaa'ina irraa kenni."
                                } else {
                                    "Microphone permission is required to record your pronunciation."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = QuizIncorrectRed
                            )
                        }

                        audioState.errorMessage?.let { err ->
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = QuizIncorrectRed
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPlayRecording,
                                enabled = audioState.hasRecordedAudio && !audioState.isRecording,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("play_recording_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOm) "Kan Koo Dhaga'i" else "Play Mine")
                            }

                            Button(
                                onClick = onCompareWithAi,
                                enabled = audioState.hasRecordedAudio &&
                                    !audioState.isRecording &&
                                    aiComparisonState !is AiModelState.Loading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("compare_ai_button")
                            ) {
                                Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOm) "AI'n Madaali" else "Compare AI")
                            }
                        }
                    }
                }

                // AI Comparison Feedback
                when (aiComparisonState) {
                    is AiModelState.Idle -> Unit
                    is AiModelState.Loading -> {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = if (isOm) {
                                        "AI sagalee kee xiinxalaa jira..."
                                    } else {
                                        "Analyzing your Afaan Oromoo pronunciation with Gemini AI..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    is AiModelState.Error -> {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("speaking_ai_error_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isOm) "Yaadachiisa AI / Intarneetii" else "AI Comparison Unavailable",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = aiComparisonState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is AiModelState.Success -> {
                        val result = aiComparisonState.data
                        val scoreColor = if (result.accuracyScore >= 75) QuizCorrectGreen else MaterialTheme.colorScheme.secondary
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, scoreColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("speaking_ai_result_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = scoreColor)
                                        Text(
                                            text = result.scoreLabel,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = scoreColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Surface(
                                        color = scoreColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "${result.accuracyScore}%",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = scoreColor,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isOm) result.feedbackOm else result.feedbackEn,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (result.phoneticTip.isNotBlank()) {
                                    Text(
                                        text = "Phonetic tip: ${result.phoneticTip}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (result.strengths.isNotEmpty()) {
                                    Text(
                                        text = if (isOm) "Cimina:" else "Strengths:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    result.strengths.forEach { s ->
                                        Text("• $s", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (result.improvements.isNotEmpty()) {
                                    Text(
                                        text = if (isOm) "Waan Fooyya'u:" else "Focus Next:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    result.improvements.forEach { imp ->
                                        Text("• $imp", style = MaterialTheme.typography.bodySmall)
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
