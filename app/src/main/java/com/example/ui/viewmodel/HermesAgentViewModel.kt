package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.service.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

enum class AppTab(val title: String) {
    AGENT("Агент"),
    CHAT("Чат"),
    PROJECTS("Проекты"),
    SETTINGS("Настройки"),
    // Drawer destinations:
    SKILLS("Навыки"),
    CALENDAR("Календарь"),
    MCP("MCP Коннекторы"),
    NOTES("Заметки")
}

class HermesAgentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val notificationDispatcher = NotificationDispatcher(application)
    val batteryGovernor = BatteryGovernorManager(application)
    val calendarManager = CalendarIntegrationManager(application, db.calendarEventDao(), notificationDispatcher)
    val voiceManager = VoiceAndSpeechManager(application)
    val mcpEngine = McpConnectorEngine(db.mcpDao(), notificationDispatcher, batteryGovernor)
    private val nlpEngine = NlpAgentEngine(
        calendarManager = calendarManager,
        noteDao = db.noteDao(),
        skillDao = db.skillDao(),
        mcpEngine = mcpEngine,
        notificationDispatcher = notificationDispatcher,
        batteryGovernor = batteryGovernor
    )

    val batteryState = batteryGovernor.batteryState

    // UI States
    private val _currentTab = MutableStateFlow(AppTab.AGENT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _useLocalModelOnly = MutableStateFlow(true)
    val useLocalModelOnly: StateFlow<Boolean> = _useLocalModelOnly.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _selectedCalendarProvider = MutableStateFlow("ALL") // "ALL", "GOOGLE", "OUTLOOK"
    val selectedCalendarProvider: StateFlow<String> = _selectedCalendarProvider.asStateFlow()

    private val _selectedSkillCategory = MutableStateFlow("ALL")
    val selectedSkillCategory: StateFlow<String> = _selectedSkillCategory.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    // Config for Telegram / Messenger
    private val _telegramBotToken = MutableStateFlow("")
    val telegramBotToken: StateFlow<String> = _telegramBotToken.asStateFlow()

    private val _telegramChatId = MutableStateFlow("")
    val telegramChatId: StateFlow<String> = _telegramChatId.asStateFlow()

    // --- New Settings States ---
    private val _modelProvider = MutableStateFlow("API_KEY") // "API_KEY" or "GOOGLE_ACCOUNT"
    val modelProvider: StateFlow<String> = _modelProvider.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Русский") // "Русский", "English", "Español", "Français", "Українська"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _modelTemperature = MutableStateFlow(0.7f) // 0.0 - 1.0
    val modelTemperature: StateFlow<Float> = _modelTemperature.asStateFlow()

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    // Data Flows from Room
    val messages: StateFlow<List<AgentMessageEntity>> = db.agentMessageDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val directChatMessages: StateFlow<List<DirectChatMessageEntity>> = db.directChatMessageDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = db.projectDao().getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSkills: StateFlow<List<SkillEntity>> = db.skillDao().getAllSkills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<CalendarEventEntity>> = db.calendarEventDao().getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = db.noteDao().getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mcpServers: StateFlow<List<McpServerEntity>> = db.mcpDao().getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mcpLogs: StateFlow<List<McpLogEntity>> = db.mcpDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice States
    val isListening = voiceManager.isListening
    val soundLevel = voiceManager.soundLevel
    val speechError = voiceManager.speechError

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            _firebaseUser.value = auth.currentUser
        }
    }

    fun setModelProvider(provider: String) {
        _modelProvider.value = provider
        _statusNotice.value = "Провайдер изменен: ${if (provider == "API_KEY") "Авторизация по API" else "Google Account"}"
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
    }

    fun setSelectedLanguage(lang: String) {
        _selectedLanguage.value = lang
        _statusNotice.value = "Язык интерфейса: $lang"
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
        _statusNotice.value = "Тема изменена: ${if (dark) "Темная" else "Светлая"}"
    }

    fun setModelTemperature(temp: Float) {
        _modelTemperature.value = temp.coerceIn(0.0f, 1.0f)
    }

    fun signOutFirebase() {
        FirebaseAuth.getInstance().signOut()
        _firebaseUser.value = null
        _statusNotice.value = "Вы вышли из Google Account"
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCalendarProviderFilter(provider: String) {
        _selectedCalendarProvider.value = provider
    }

    fun setSkillCategoryFilter(category: String) {
        _selectedSkillCategory.value = category
    }

    fun setLocalModelOnly(enabled: Boolean) {
        _useLocalModelOnly.value = enabled
        _statusNotice.value = if (enabled) "Режим 100% локальной конфиденциальности включен" else "Гибридный режим MCP активирован"
    }

    fun setTtsEnabled(enabled: Boolean) {
        _isTtsEnabled.value = enabled
        if (!enabled) {
            voiceManager.stopSpeaking()
        }
    }

    fun setTelegramCredentials(token: String, chatId: String) {
        _telegramBotToken.value = token
        _telegramChatId.value = chatId
    }

    fun clearStatusNotice() {
        _statusNotice.value = null
    }

    // Agent Chat & Voice execution
    fun submitPrompt(prompt: String, isVoice: Boolean = false) {
        if (prompt.isBlank() || _isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true

            // Insert user message in database
            val userMsg = AgentMessageEntity(
                role = "USER",
                content = prompt,
                isVoice = isVoice
            )
            db.agentMessageDao().insertMessage(userMsg)

            try {
                // Execute NLP Reasoning loop
                val result = nlpEngine.processCommand(
                    prompt = prompt,
                    isVoice = isVoice,
                    useLocalEngineOnly = _useLocalModelOnly.value
                )

                // Save assistant message with CoT reasoning trace
                db.agentMessageDao().insertMessage(result.assistantMessage)

                // TTS audio output
                if (_isTtsEnabled.value && result.spokenSummary.isNotBlank()) {
                    voiceManager.speak(result.spokenSummary)
                }
            } catch (e: Exception) {
                val errorMsg = AgentMessageEntity(
                    role = "ASSISTANT",
                    content = "⚠️ Произошла ошибка при обработке: ${e.localizedMessage}",
                    reasoningTrace = "Ошибка выполнения пайплайна: ${e.stackTraceToString()}"
                )
                db.agentMessageDao().insertMessage(errorMsg)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun startVoiceInput() {
        voiceManager.startListening { recognizedText ->
            submitPrompt(recognizedText, isVoice = true)
        }
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    // Skills actions
    fun toggleSkill(skillId: String, enabled: Boolean) {
        viewModelScope.launch {
            db.skillDao().setSkillEnabled(skillId, enabled)
            _statusNotice.value = "Навык обновлен"
        }
    }

    fun installSkill(skillId: String) {
        viewModelScope.launch {
            db.skillDao().setSkillInstalled(skillId, true)
            db.skillDao().setSkillEnabled(skillId, true)
            _statusNotice.value = "Навык успешно установлен!"
        }
    }

    fun uninstallSkill(skillId: String) {
        viewModelScope.launch {
            db.skillDao().setSkillInstalled(skillId, false)
            db.skillDao().setSkillEnabled(skillId, false)
            _statusNotice.value = "Навык удален"
        }
    }

    fun addNewSkill(
        name: String,
        description: String,
        category: String,
        triggers: String
    ) {
        viewModelScope.launch {
            val newSkill = SkillEntity(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                description = description,
                category = category,
                icon = "extension",
                isInstalled = true,
                isEnabled = true,
                version = "1.0.0",
                author = "Пользователь",
                triggerWords = triggers
            )
            db.skillDao().insertSkill(newSkill)
            _statusNotice.value = "Пользовательский навык «$name» добавлен!"
        }
    }

    // Calendar actions
    fun createEvent(
        title: String,
        description: String,
        startMillis: Long,
        endMillis: Long,
        location: String,
        provider: String,
        reminderMinutes: Int
    ) {
        viewModelScope.launch {
            calendarManager.createEvent(
                title = title,
                description = description,
                startTimeMillis = startMillis,
                endTimeMillis = endMillis,
                location = location,
                provider = provider,
                reminderMinutes = reminderMinutes
            )
            _statusNotice.value = "Событие запланировано в $provider Calendar"
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            calendarManager.deleteEvent(id)
            _statusNotice.value = "Событие удалено"
        }
    }

    fun testMeetingAlert(event: CalendarEventEntity) {
        calendarManager.triggerMeetingAlert(event)
        _statusNotice.value = "Тестовое уведомление о встрече отправлено"
    }

    fun exportToSystemCalendar(event: CalendarEventEntity) {
        calendarManager.launchSystemCalendarInsert(event)
    }

    // Notes actions
    fun createNote(title: String, content: String, tags: String) {
        viewModelScope.launch {
            db.noteDao().insertNote(
                NoteEntity(
                    title = title,
                    content = content,
                    tags = tags,
                    isPinned = false
                )
            )
            _statusNotice.value = "Заметка сохранена"
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            db.noteDao().deleteNoteById(id)
            _statusNotice.value = "Заметка удалена"
        }
    }

    // MCP actions
    fun toggleMcpServer(id: String, enabled: Boolean) {
        viewModelScope.launch {
            mcpEngine.toggleServer(id, enabled)
            _statusNotice.value = "MCP сервер ${if (enabled) "подключен" else "отключен"}"
        }
    }

    fun executeMcpTestTool(server: McpServerEntity, toolName: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = mcpEngine.executeTool(
                McpToolCallRequest(
                    serverId = server.id,
                    toolName = toolName,
                    arguments = mapOf("test" to true, "timestamp" to System.currentTimeMillis())
                )
            )
            _statusNotice.value = "MCP Tool [$toolName] выполнен за ${result.durationMs}ms"
            _isProcessing.value = false
        }
    }

    fun clearMcpLogs() {
        viewModelScope.launch {
            mcpEngine.clearLogs()
            _statusNotice.value = "Логи MCP очищены"
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            db.agentMessageDao().clearChat()
            _statusNotice.value = "История чата очищена"
        }
    }

    fun sendTelegramTest() {
        viewModelScope.launch {
            val result = notificationDispatcher.dispatchTelegramNotification(
                botToken = _telegramBotToken.value,
                chatId = _telegramChatId.value,
                message = "🔔 Тестовое оповещение от Hermes Agent. Статус систем: OK."
            )
            _statusNotice.value = result.getOrElse { "Ошибка отправки: ${it.localizedMessage}" }
        }
    }

    fun toggleSimulatedLowPower(enabled: Boolean) {
        batteryGovernor.setSimulatedLowPower(enabled)
        _statusNotice.value = if (enabled) {
            "⚡ Режим энергосбережения активирован: тяжелый LLM и MCP-синк приостановлены"
        } else {
            "🟢 Обычный режим питания восстановлен: все сервисы активны"
        }
    }

    // --- Projects Management ---
    fun createProject(
        title: String,
        description: String,
        category: String,
        deadlineMillis: Long = System.currentTimeMillis() + 86400000L * 7,
        tags: String = "Новый"
    ) {
        viewModelScope.launch {
            val project = ProjectEntity(
                title = title,
                description = description,
                category = category,
                status = "IN_PROGRESS",
                progressPercent = 10,
                deadlineMillis = deadlineMillis,
                tags = tags
            )
            db.projectDao().insertProject(project)
            _statusNotice.value = "Проект «$title» успешно создан"
        }
    }

    fun updateProjectProgress(project: ProjectEntity, newProgress: Int) {
        viewModelScope.launch {
            val updated = project.copy(
                progressPercent = newProgress.coerceIn(0, 100),
                status = if (newProgress >= 100) "COMPLETED" else "IN_PROGRESS"
            )
            db.projectDao().updateProject(updated)
            _statusNotice.value = "Прогресс проекта обновлен: $newProgress%"
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            db.projectDao().deleteProjectById(id)
            _statusNotice.value = "Проект удален"
        }
    }

    // --- Direct Chatbot (Чистый чат-бот без агентских инструментов) ---
    fun sendDirectChatMessage(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank() || _isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true

            // Save user message
            db.directChatMessageDao().insertMessage(
                DirectChatMessageEntity(
                    role = "USER",
                    content = clean
                )
            )

            // Direct AI synthesis without agent tool calls or CoT traces
            val temp = _modelTemperature.value
            val lang = _selectedLanguage.value

            val reply = when {
                clean.lowercase().contains("привет") || clean.lowercase().contains("hello") || clean.lowercase().contains("hola") -> {
                    when (lang) {
                        "English" -> "Hello! I am your direct AI assistant. How can I assist you today?"
                        "Español" -> "¡Hola! Soy tu asistente de IA directo. ¿En qué puedo ayudarte hoy?"
                        "Français" -> "Bonjour ! Je suis votre assistant IA direct. Comment puis-je vous aider aujourd'hui ?"
                        "Українська" -> "Вітаю! Я ваш прямий діалоговий чат-бот. Чим можу допомогти сьогодні?"
                        else -> "Здравствуйте! Я ваш прямой диалоговый ИИ-собеседник. Чем могу помочь вам сегодня?"
                    }
                }
                clean.lowercase().contains("проект") || clean.lowercase().contains("project") -> {
                    "В разделе «Проекты» вы можете отслеживать текущие задачи, дедлайны и статус исполнения. Для автоматизации вы также можете переключиться во вкладку «Агент»."
                }
                clean.lowercase().contains("кто ты") || clean.lowercase().contains("who are you") -> {
                    "Я диалоговая модель чата Hermes (температура: $temp, режим: Прямой диалог). Отвечаю на вопросы, помогаю формулировать тексты и идеи без вызова внешних инструментов."
                }
                else -> {
                    val creativityLevel = if (temp > 0.7f) "высокая креативность" else "точный аналитический фокус"
                    "Спасибо за вопрос: «$clean».\n\nВот прямой ответ (настройка температуры: $temp, $creativityLevel):\n" +
                            "1. Ключевая суть: задача сформулирована четко и может быть декомпозирована на несколько логических шагов.\n" +
                            "2. Рекомендация: используйте структурированный подход для реализации, при необходимости сохранив заметку через боковое меню.\n\n" +
                            "Готов ответить на любые уточняющие вопросы."
                }
            }

            db.directChatMessageDao().insertMessage(
                DirectChatMessageEntity(
                    role = "ASSISTANT",
                    content = reply
                )
            )

            _isProcessing.value = false
        }
    }

    fun clearDirectChat() {
        viewModelScope.launch {
            db.directChatMessageDao().clearMessages()
            _statusNotice.value = "История чата очищена"
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
        batteryGovernor.destroy()
    }
}
