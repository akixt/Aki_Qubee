package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLanguage
import com.example.data.OromoContent

private val searchStarterTopics = listOf("Nagaa", "Buna", "Dhugaa", "Market", "Tokkummaa", "Water")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UnifiedSearchModal(
    uiState: QubeeUiState,
    onQueryChange: (String) -> Unit,
    onSaveSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSpeakText: (String) -> Unit,
    onSelectProverb: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val q = uiState.unifiedSearchQuery.trim().lowercase()

    val qubeeMatches = remember(q) {
        if (q.isEmpty()) emptyList() else OromoContent.qubeeLetters.filter {
            it.name.lowercase().contains(q) || it.exampleWord.lowercase().contains(q) || it.exampleMeaningEn.lowercase().contains(q)
        }.take(6)
    }
    val vocabMatches = remember(q, uiState.allVocabulary) {
        if (q.isEmpty()) emptyList() else uiState.allVocabulary.filter {
            it.oromo.lowercase().contains(q) || it.english.lowercase().contains(q)
        }.take(20)
    }
    val phraseMatches = remember(q, uiState.allPhrases) {
        if (q.isEmpty()) emptyList() else uiState.allPhrases.filter {
            it.oromo.lowercase().contains(q) || it.english.lowercase().contains(q)
        }.take(20)
    }
    val proverbMatches = remember(q, uiState.allProverbs) {
        if (q.isEmpty()) emptyList() else uiState.allProverbs.filter {
            it.oromo.lowercase().contains(q) || it.english.lowercase().contains(q) || it.meaningEn.lowercase().contains(q)
        }.take(20)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("unified_search_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            text = if (isOm) "Barbaacha Waliigalaa" else "Search Everything",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close search")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.unifiedSearchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        if (uiState.unifiedSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = if (isOm) "Haqi" else "Clear")
                            }
                        }
                    },
                    placeholder = {
                        Text(if (isOm) "Jechoota, haasawa, mammaaksa ykn qubee barbaadi..." else "Search vocabulary, phrases, proverbs, Qubee...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unified_search_input")
                )

                if (q.isEmpty()) {
                    if (uiState.searchHistory.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOm) "Barbaacha Dhiyeenyaa" else "Recent Searches",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = onClearHistory) {
                                Text(if (isOm) "Haqi" else "Clear")
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.searchHistory.forEach { item ->
                                SuggestionChip(
                                    icon = { Icon(Icons.Filled.History, null, modifier = Modifier.size(16.dp)) },
                                    onClick = { onQueryChange(item.query) },
                                    label = { Text(item.query) }
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isOm) "Yaada Barbaachaa" else "Try Searching",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        searchStarterTopics.forEach { topic ->
                            SuggestionChip(
                                onClick = {
                                    onQueryChange(topic)
                                    onSaveSearch(topic)
                                },
                                label = { Text(topic) }
                            )
                        }
                    }
                } else {
                    val total = qubeeMatches.size + vocabMatches.size + phraseMatches.size + proverbMatches.size
                    if (total == 0) {
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.SearchOff, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isOm) "Wanti argame hin jiru." else "No results found for \"$q\"")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (vocabMatches.isNotEmpty()) {
                                item {
                                    Text(if (isOm) "Jechoota (${vocabMatches.size})" else "Vocabulary (${vocabMatches.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                items(vocabMatches, key = { "us_v_${it.id}" }) { item ->
                                    SearchResultRow(
                                        title = "${item.oromo} — ${item.english}",
                                        subtitle = item.exampleOm,
                                        onSpeak = { onSaveSearch(q); onSpeakText(item.oromo) },
                                        onClick = { onSaveSearch(q); onSpeakText(item.oromo) }
                                    )
                                }
                            }
                            if (phraseMatches.isNotEmpty()) {
                                item {
                                    Text(if (isOm) "Haasawa (${phraseMatches.size})" else "Conversations (${phraseMatches.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                items(phraseMatches, key = { "us_p_${it.id}" }) { item ->
                                    SearchResultRow(
                                        title = item.oromo,
                                        subtitle = item.english,
                                        onSpeak = { onSaveSearch(q); onSpeakText(item.oromo) },
                                        onClick = { onSaveSearch(q); onSpeakText(item.oromo) }
                                    )
                                }
                            }
                            if (proverbMatches.isNotEmpty()) {
                                item {
                                    Text(if (isOm) "Mammaaksa (${proverbMatches.size})" else "Proverbs (${proverbMatches.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                items(proverbMatches, key = { "us_m_${it.id}" }) { item ->
                                    SearchResultRow(
                                        title = item.oromo,
                                        subtitle = item.english,
                                        onSpeak = { onSaveSearch(q); onSpeakText(item.oromo) },
                                        onClick = {
                                            onSaveSearch(q)
                                            onSelectProverb(item.id)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                            if (qubeeMatches.isNotEmpty()) {
                                item {
                                    Text("Qubee (${qubeeMatches.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                items(qubeeMatches, key = { "us_q_${it.name}" }) { item ->
                                    SearchResultRow(
                                        title = "${item.symbol} (${item.name})",
                                        subtitle = "${item.exampleWord} — ${item.exampleMeaningEn}",
                                        onSpeak = { onSaveSearch(q); onSpeakText(item.ttsPronunciation) },
                                        onClick = { onSaveSearch(q); onSpeakText(item.ttsPronunciation) }
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

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    onSpeak: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onSpeak) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen")
            }
        }
    }
}
