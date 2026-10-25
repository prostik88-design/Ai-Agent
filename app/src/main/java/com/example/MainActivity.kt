package com.example

import android.Manifest
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.HermesAgentViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HermesAgentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
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
    val directChatMessages by viewModel.directChatMessages.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val automationWorkflows by viewModel.automationWorkflows.collectAsStateWithLifecycle()
    val workflowLogs by viewModel.workflowLogs.collectAsStateWithLifecycle()
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
    val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()

    // Settings States
    val modelProvider by viewModel.modelProvider.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val modelTemperature by viewModel.modelTemperature.collectAsStateWithLifecycle()
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Runtime Permission Launcher for Microphone and Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!audioGranted) {
            // Text mode fallback
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

    // BackHandler: Close drawer if open, else return to Agent tab
    BackHandler(enabled = drawerState.isOpen || currentTab != AppTab.AGENT) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.setTab(AppTab.AGENT)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDarkTheme) AgentSurfaceDark else Color.White,
                drawerContentColor = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) AgentVoidDark else Color(0xFFE2E8F0))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AgentCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = AgentVoidDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "HERMES AGENT",
                                color = if (isDarkTheme) AgentCyan else Color(0xFF0284C7),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Модули & Интеграции",
                                color = AgentTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (firebaseUser != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFF4285F4).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = firebaseUser?.email ?: "Google Account",
                                    color = Color(0xFF4285F4),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "РАБОЧИЕ ИНСТРУМЕНТЫ",
                    color = AgentTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // Drawer items: Навыки, Календарь, MCP, Заметки
                NavigationDrawerItem(
                    label = { Text("⚡ Навыки (${skills.count { it.isInstalled }}/${skills.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Extension, contentDescription = null) },
                    selected = currentTab == AppTab.SKILLS,
                    onClick = {
                        viewModel.setTab(AppTab.SKILLS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("drawer_skills")
                )

                NavigationDrawerItem(
                    label = { Text("📅 Календарь (${calendarEvents.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    selected = currentTab == AppTab.CALENDAR,
                    onClick = {
                        viewModel.setTab(AppTab.CALENDAR)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("drawer_calendar")
                )

                NavigationDrawerItem(
                    label = { Text("🔌 MCP Коннекторы (${mcpServers.count { it.isEnabled }})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Hub, contentDescription = null) },
                    selected = currentTab == AppTab.MCP,
                    onClick = {
                        viewModel.setTab(AppTab.MCP)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("drawer_mcp")
                )

                NavigationDrawerItem(
                    label = { Text("📝 Заметки & База (${notes.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    selected = currentTab == AppTab.NOTES,
                    onClick = {
                        viewModel.setTab(AppTab.NOTES)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("drawer_notes")
                )

                Spacer(modifier = Modifier.weight(1f))

                Divider(color = AgentCardStrokeDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hermes OS v2.5",
                        color = AgentTextTertiary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${batteryState.levelPercent}% АКБ",
                        color = if (batteryState.isSuspended) AgentAmber else AgentEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                AgentHeader(
                    isLocalOnly = useLocalModelOnly,
                    activeMcpCount = mcpServers.count { it.isEnabled },
                    isProcessing = isProcessing,
                    batteryState = batteryState,
                    activeWorkflowsCount = automationWorkflows.count { it.isEnabled },
                    onPrivacyToggle = {
                        viewModel.setLocalModelOnly(!useLocalModelOnly)
                    },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (isDarkTheme) AgentSurfaceDark else Color.White,
                    contentColor = AgentCyan,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(width = 1.dp, color = if (isDarkTheme) AgentCardStrokeDark else Color(0xFFE2E8F0))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationItem(
                        label = "Агент",
                        icon = Icons.Default.SmartToy,
                        isSelected = currentTab == AppTab.AGENT,
                        testTag = "nav_agent",
                        onClick = { viewModel.setTab(AppTab.AGENT) }
                    )
                    NavigationItem(
                        label = "Чат",
                        icon = Icons.Default.ChatBubble,
                        isSelected = currentTab == AppTab.CHAT,
                        testTag = "nav_chat",
                        onClick = { viewModel.setTab(AppTab.CHAT) }
                    )
                    NavigationItem(
                        label = "Проекты",
                        icon = Icons.Default.FolderSpecial,
                        isSelected = currentTab == AppTab.PROJECTS,
                        testTag = "nav_projects",
                        onClick = { viewModel.setTab(AppTab.PROJECTS) }
                    )
                    NavigationItem(
                        label = "Настройки",
                        icon = Icons.Default.Settings,
                        isSelected = currentTab == AppTab.SETTINGS,
                        testTag = "nav_settings",
                        onClick = { viewModel.setTab(AppTab.SETTINGS) }
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
            containerColor = if (isDarkTheme) AgentVoidDark else Color(0xFFF8FAFC)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.AGENT -> AgentChatScreen(
                        messages = messages,
                        isProcessing = isProcessing,
                        isListening = isListening,
                        soundLevel = soundLevel,
                        speechError = speechError,
                        batteryState = batteryState,
                        onSendMessage = { prompt, isVoice -> viewModel.submitPrompt(prompt, isVoice) },
                        onStartVoice = { viewModel.startVoiceInput() },
                        onStopVoice = { viewModel.stopVoiceInput() },
                        onSpeakText = { text -> viewModel.speakText(text) },
                        onClearChat = { viewModel.clearChat() }
                    )
                    AppTab.CHAT -> DirectChatScreen(
                        messages = directChatMessages,
                        isProcessing = isProcessing,
                        isDarkTheme = isDarkTheme,
                        onSendMessage = { prompt -> viewModel.sendDirectChatMessage(prompt) },
                        onClearChat = { viewModel.clearDirectChat() }
                    )
                    AppTab.PROJECTS -> ProjectsScreen(
                        projects = projects,
                        workflows = automationWorkflows,
                        workflowLogs = workflowLogs,
                        isDarkTheme = isDarkTheme,
                        onCreateProject = { title, desc, cat, tags -> viewModel.createProject(title, desc, cat, tags = tags) },
                        onUpdateProgress = { project, progress -> viewModel.updateProjectProgress(project, progress) },
                        onDeleteProject = { id -> viewModel.deleteProject(id) },
                        onToggleWorkflow = { id, enabled -> viewModel.toggleWorkflow(id, enabled) },
                        onExecuteWorkflow = { wf -> viewModel.executeWorkflow(wf) },
                        onCreateWorkflow = { title, desc, cat, trigType, trigVal, actions ->
                            viewModel.createWorkflow(title, desc, cat, trigType, trigVal, actions)
                        },
                        onDeleteWorkflow = { id -> viewModel.deleteWorkflow(id) }
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        modelProvider = modelProvider,
                        apiKey = apiKey,
                        selectedLanguage = selectedLanguage,
                        isDarkTheme = isDarkTheme,
                        modelTemperature = modelTemperature,
                        firebaseUser = firebaseUser,
                        batteryState = batteryState,
                        useLocalModelOnly = useLocalModelOnly,
                        isTtsEnabled = isTtsEnabled,
                        telegramBotToken = telegramBotToken,
                        telegramChatId = telegramChatId,
                        onModelProviderChange = { prov -> viewModel.setModelProvider(prov) },
                        onApiKeyChange = { key -> viewModel.setApiKey(key) },
                        onLanguageChange = { lang -> viewModel.setSelectedLanguage(lang) },
                        onThemeChange = { dark -> viewModel.setDarkTheme(dark) },
                        onTemperatureChange = { temp -> viewModel.setModelTemperature(temp) },
                        onSignOutFirebase = { viewModel.signOutFirebase() },
                        onToggleLocalModel = { enabled -> viewModel.setLocalModelOnly(enabled) },
                        onToggleTts = { enabled -> viewModel.setTtsEnabled(enabled) },
                        onToggleSimulatedLowPower = { enabled -> viewModel.toggleSimulatedLowPower(enabled) },
                        onSaveTelegramCredentials = { token, chat -> viewModel.setTelegramCredentials(token, chat) },
                        onSendTelegramTest = { viewModel.sendTelegramTest() }
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
                    AppTab.NOTES -> NotesScreen(
                        notes = notes,
                        isDarkTheme = isDarkTheme,
                        onCreateNote = { title, content, tags -> viewModel.createNote(title, content, tags) },
                        onDeleteNote = { id -> viewModel.deleteNote(id) }
                    )
                }
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
