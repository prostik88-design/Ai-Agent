package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.HermesAgentViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HermesAgentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: HermesAgentViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val useLocalModelOnly by viewModel.useLocalModelOnly.collectAsStateWithLifecycle()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsStateWithLifecycle()
    val statusNotice by viewModel.statusNotice.collectAsStateWithLifecycle()

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val skills by viewModel.allSkills.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val mcpServers by viewModel.mcpServers.collectAsStateWithLifecycle()
    val mcpLogs by viewModel.mcpLogs.collectAsStateWithLifecycle()

    val selectedCalProvider by viewModel.selectedCalendarProvider.collectAsStateWithLifecycle()
    val selectedSkillCategory by viewModel.selectedSkillCategory.collectAsStateWithLifecycle()

    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val soundLevel by viewModel.soundLevel.collectAsStateWithLifecycle()
    val speechError by viewModel.speechError.collectAsStateWithLifecycle()

    val telegramBotToken by viewModel.telegramBotToken.collectAsStateWithLifecycle()
    val telegramChatId by viewModel.telegramChatId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Runtime Permission Launcher for Microphone and Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!audioGranted) {
            // Can still use text mode
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Show status notice via Snackbar
    LaunchedEffect(statusNotice) {
        statusNotice?.let { notice ->
            snackbarHostState.showSnackbar(
                message = notice,
                duration = SnackbarDuration.Short
            )
            viewModel.clearStatusNotice()
        }
    }

    // BackHandler: Return to Chat tab if on another screen
    BackHandler(enabled = currentTab != AppTab.CHAT) {
        viewModel.setTab(AppTab.CHAT)
    }

    Scaffold(
        topBar = {
            AgentHeader(
                isLocalOnly = useLocalModelOnly,
                activeMcpCount = mcpServers.count { it.isEnabled },
                isProcessing = isProcessing,
                onPrivacyToggle = {
                    viewModel.setLocalModelOnly(!useLocalModelOnly)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AgentSurfaceDark,
                contentColor = AgentCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = AgentCardStrokeDark)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationItem(
                    label = "Агент",
                    icon = Icons.Default.SmartToy,
                    isSelected = currentTab == AppTab.CHAT,
                    testTag = "nav_chat",
                    onClick = { viewModel.setTab(AppTab.CHAT) }
                )
                NavigationItem(
                    label = "Навыки",
                    icon = Icons.Default.Extension,
                    isSelected = currentTab == AppTab.SKILLS,
                    testTag = "nav_skills",
                    onClick = { viewModel.setTab(AppTab.SKILLS) }
                )
                NavigationItem(
                    label = "Календарь",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = currentTab == AppTab.CALENDAR,
                    testTag = "nav_calendar",
                    onClick = { viewModel.setTab(AppTab.CALENDAR) }
                )
                NavigationItem(
                    label = "MCP",
                    icon = Icons.Default.Hub,
                    isSelected = currentTab == AppTab.MCP,
                    testTag = "nav_mcp",
                    onClick = { viewModel.setTab(AppTab.MCP) }
                )
                NavigationItem(
                    label = "Заметки",
                    icon = Icons.Default.Shield,
                    isSelected = currentTab == AppTab.NOTES_SETTINGS,
                    testTag = "nav_notes_settings",
                    onClick = { viewModel.setTab(AppTab.NOTES_SETTINGS) }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    containerColor = AgentSurfaceVariantDark,
                    contentColor = AgentTextPrimary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = data.visuals.message, fontSize = 13.sp)
                }
            }
        },
        containerColor = AgentVoidDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.CHAT -> AgentChatScreen(
                    messages = messages,
                    isProcessing = isProcessing,
                    isListening = isListening,
                    soundLevel = soundLevel,
                    speechError = speechError,
                    onSendMessage = { prompt, isVoice -> viewModel.submitPrompt(prompt, isVoice) },
                    onStartVoice = { viewModel.startVoiceInput() },
                    onStopVoice = { viewModel.stopVoiceInput() },
                    onSpeakText = { text -> viewModel.speakText(text) },
                    onClearChat = { viewModel.clearChat() }
                )
                AppTab.SKILLS -> SkillsScreen(
                    skills = skills,
                    selectedCategory = selectedSkillCategory,
                    onCategorySelected = { cat -> viewModel.setSkillCategoryFilter(cat) },
                    onToggleSkill = { id, enabled -> viewModel.toggleSkill(id, enabled) },
                    onInstallSkill = { id -> viewModel.installSkill(id) },
                    onUninstallSkill = { id -> viewModel.uninstallSkill(id) },
                    onAddNewSkill = { name, desc, cat, triggers -> viewModel.addNewSkill(name, desc, cat, triggers) }
                )
                AppTab.CALENDAR -> CalendarScreen(
                    events = calendarEvents,
                    selectedProvider = selectedCalProvider,
                    onProviderSelected = { prov -> viewModel.setCalendarProviderFilter(prov) },
                    onCreateEvent = { title, desc, start, end, loc, prov, rem ->
                        viewModel.createEvent(title, desc, start, end, loc, prov, rem)
                    },
                    onDeleteEvent = { id -> viewModel.deleteEvent(id) },
                    onTestAlert = { event -> viewModel.testMeetingAlert(event) },
                    onExportToSystem = { event -> viewModel.exportToSystemCalendar(event) }
                )
                AppTab.MCP -> McpScreen(
                    servers = mcpServers,
                    logs = mcpLogs,
                    onToggleServer = { id, enabled -> viewModel.toggleMcpServer(id, enabled) },
                    onTestTool = { server, tool -> viewModel.executeMcpTestTool(server, tool) },
                    onClearLogs = { viewModel.clearMcpLogs() }
                )
                AppTab.NOTES_SETTINGS -> NotesAndSettingsScreen(
                    notes = notes,
                    useLocalModelOnly = useLocalModelOnly,
                    isTtsEnabled = isTtsEnabled,
                    telegramBotToken = telegramBotToken,
                    telegramChatId = telegramChatId,
                    onToggleLocalModel = { enabled -> viewModel.setLocalModelOnly(enabled) },
                    onToggleTts = { enabled -> viewModel.setTtsEnabled(enabled) },
                    onSaveTelegramCredentials = { token, chat -> viewModel.setTelegramCredentials(token, chat) },
                    onSendTelegramTest = { viewModel.sendTelegramTest() },
                    onCreateNote = { title, content, tags -> viewModel.createNote(title, content, tags) },
                    onDeleteNote = { id -> viewModel.deleteNote(id) }
                )
            }
        }
    }
}

@Composable
fun RowScope.NavigationItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AgentVoidDark,
            selectedTextColor = AgentCyan,
            indicatorColor = AgentCyan,
            unselectedIconColor = AgentTextSecondary,
            unselectedTextColor = AgentTextSecondary
        ),
        modifier = Modifier.testTag(testTag)
    )
}
