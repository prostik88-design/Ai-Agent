package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.service.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    CHAT("Агент"),
    SKILLS("Навыки"),
    CALENDAR("Календарь"),
    MCP("MCP Коннекторы"),
    NOTES_SETTINGS("Заметки & Приватность")
}

class HermesAgentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val notificationDispatcher = NotificationDispatcher(application)
    val calendarManager = CalendarIntegrationManager(application, db.calendarEventDao(), notificationDispatcher)
    val voiceManager = VoiceAndSpeechManager(application)
    val mcpEngine = McpConnectorEngine(db.mcpDao(), notificationDispatcher)
    private val nlpEngine = NlpAgentEngine(
        calendarManager = calendarManager,
        noteDao = db.noteDao(),
        skillDao = db.skillDao(),
        mcpEngine = mcpEngine,
        notificationDispatcher = notificationDispatcher
    )

    // UI States
    private val _currentTab = MutableStateFlow(AppTab.CHAT)
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

    // Data Flows from Room
    val messages: StateFlow<List<AgentMessageEntity>> = db.agentMessageDao().getAllMessages()
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

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
