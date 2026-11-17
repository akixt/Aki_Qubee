package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.AppLanguage
import com.example.data.ProverbItem
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_QUBEE = "qubee"
const val ROUTE_PHRASES = "phrases"

private data class NavDestination(
    val route: String,
    val labelOmRes: Int,
    val labelEnRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val destinations = listOf(
    NavDestination(
        route = ROUTE_HOME,
        labelOmRes = R.string.nav_home_om,
        labelEnRes = R.string.nav_home_en,
        selectedIcon = Icons.Filled.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories,
        testTag = "nav_home_tab"
    ),
    NavDestination(
        route = ROUTE_QUBEE,
        labelOmRes = R.string.nav_qubee_om,
        labelEnRes = R.string.nav_qubee_en,
        selectedIcon = Icons.Filled.Abc,
        unselectedIcon = Icons.Outlined.Abc,
        testTag = "nav_qubee_tab"
    ),
    NavDestination(
        route = ROUTE_PHRASES,
        labelOmRes = R.string.nav_phrases_om,
        labelEnRes = R.string.nav_phrases_en,
        selectedIcon = Icons.Filled.Forum,
        unselectedIcon = Icons.Outlined.Forum,
        testTag = "nav_phrases_tab"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QubeeApp(
    uiState: QubeeUiState,
    isDarkTheme: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onNextProverb: () -> Unit,
    onSelectProverb: (Int) -> Unit,
    onSelectQubeeFilter: (com.example.data.QubeeCategory?) -> Unit,
    onSelectLetter: (com.example.data.QubeeLetter) -> Unit,
    onSearchPhrases: (String) -> Unit,
    onSelectPhraseCategory: (com.example.data.PhraseCategory?) -> Unit,
    onSpeakText: (String) -> Boolean
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isOm = uiState.language == AppLanguage.OROMO

    val copySuccessMessage = stringResource(
        if (isOm) R.string.copy_success_om else R.string.copy_success_en
    )
    val ttsUnavailableMessage = if (isOm) {
        "Sagaleen dubbisaa meeshaa kanarratti hin qophoofne."
    } else {
        "Voice playback is unavailable on this device."
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Qubee", text))
        scope.launch {
            snackbarHostState.showSnackbar(copySuccessMessage)
        }
    }

    fun shareProverb(proverb: ProverbItem) {
        val shareText = "“${proverb.oromo}”\n— ${proverb.english}\n\n(${if (isOm) proverb.meaningOm else proverb.meaningEn})"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, if (isOm) "Mammaaksa Qoodi" else "Share Proverb"))
    }

    fun handleSpeak(text: String) {
        val started = onSpeakText(text)
        if (!started) {
            scope.launch {
                snackbarHostState.showSnackbar(ttsUnavailableMessage)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = stringResource(id = R.string.theme_toggle_cd)
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.aki_logo),
                                contentDescription = stringResource(id = R.string.brand_logo_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .testTag("topbar_brand_logo")
                            )
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = onToggleLanguage,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("language_toggle_button")
                        ) {
                            Text(
                                text = if (isOm) "OM ⇄ EN" else "EN ⇄ OM",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(
                                if (isOm) dest.labelOmRes else dest.labelEnRes
                            )
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(text = label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(
                                if (isOm) dest.labelOmRes else dest.labelEnRes
                            )
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(text = label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                uiState = uiState,
                                onNextProverb = onNextProverb,
                                onSelectProverb = onSelectProverb,
                                onCopyText = ::copyToClipboard,
                                onShareProverb = ::shareProverb,
                                onNavigateToQubee = { navigateTo(ROUTE_QUBEE) },
                                onNavigateToPhrases = { navigateTo(ROUTE_PHRASES) }
                            )
                        }
                        composable(ROUTE_QUBEE) {
                            QubeeAlphabetScreen(
                                uiState = uiState,
                                onSelectFilter = onSelectQubeeFilter,
                                onSelectLetter = onSelectLetter,
                                onSpeakWord = ::handleSpeak
                            )
                        }
                        composable(ROUTE_PHRASES) {
                            PhrasesScreen(
                                uiState = uiState,
                                onSearchChange = onSearchPhrases,
                                onSelectCategory = onSelectPhraseCategory,
                                onCopyPhrase = ::copyToClipboard,
                                onSpeakPhrase = ::handleSpeak
                            )
                        }
                    }
                }
            }
        }
    }
}
