package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SkillEntity::class,
        CalendarEventEntity::class,
        NoteEntity::class,
        McpServerEntity::class,
        McpLogEntity::class,
        AgentMessageEntity::class,
        AgentTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun skillDao(): SkillDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun noteDao(): NoteDao
    abstract fun mcpDao(): McpDao
    abstract fun agentMessageDao(): AgentMessageDao
    abstract fun agentTaskDao(): AgentTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hermes_agent.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                populateInitialData(database)
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Initial Skills
            val defaultSkills = listOf(
                SkillEntity(
                    id = "email_manager",
                    name = "Управление Email",
                    description = "Автоматический разбор почты, составление ответов, поиск важных писем и дайджест входящих сообщений.",
                    category = "EMAIL",
                    icon = "mail",
                    isInstalled = true,
                    isEnabled = true,
                    version = "2.1.0",
                    author = "Hermes Core",
                    triggerWords = "письмо,почта,email,напиши ответ,черновик,проверь почту"
                ),
                SkillEntity(
                    id = "notes_organizer",
                    name = "Заметки и База знаний",
                    description = "Создание структурированных заметок, фиксация идей, извлечение задач (Action Items) и тегирование.",
                    category = "NOTES",
                    icon = "edit_note",
                    isInstalled = true,
                    isEnabled = true,
                    version = "1.8.4",
                    author = "Hermes Core",
                    triggerWords = "заметка,запиши,зафиксируй,идея,список дел,план"
                ),
                SkillEntity(
                    id = "web_search",
                    name = "Поиск информации в Сети",
                    description = "Глубокий интернет-поиск, анализ документации, извлечение фактов, агрегация новостей без спама.",
                    category = "SEARCH",
                    icon = "travel_explore",
                    isInstalled = true,
                    isEnabled = true,
                    version = "3.0.1",
                    author = "Hermes Core",
                    triggerWords = "найди,поищи в интернете,поиск,узнай,новости,статья,гугл"
                ),
                SkillEntity(
                    id = "google_calendar",
                    name = "Google Calendar Sync",
                    description = "Двусторонняя интеграция с Google Календарем: создание встреч, перенос, удаление и уведомления по голосу.",
                    category = "CALENDAR",
                    icon = "calendar_month",
                    isInstalled = true,
                    isEnabled = true,
                    version = "2.4.0",
                    author = "Google Workspace MCP",
                    triggerWords = "google календарь,гугл календарь,встреча гугл,расписание"
                ),
                SkillEntity(
                    id = "outlook_calendar",
                    name = "Outlook Calendar Sync",
                    description = "Управление корпоративным календарем Microsoft Outlook & Exchange, синхронизация созвонов и коллизий.",
                    category = "CALENDAR",
                    icon = "event_available",
                    isInstalled = true,
                    isEnabled = true,
                    version = "2.2.0",
                    author = "Microsoft 365 MCP",
                    triggerWords = "аутлук,outlook,корпоративная встреча,митинг,microsoft"
                ),
                SkillEntity(
                    id = "telegram_dispatcher",
                    name = "Telegram & Мессенджеры",
                    description = "Мгновенные уведомления о дедлайнах, отправка дайджеста дня в Telegram бота и командные алерты.",
                    category = "MESSENGER",
                    icon = "send",
                    isInstalled = true,
                    isEnabled = true,
                    version = "1.5.0",
                    author = "Hermes Social",
                    triggerWords = "телеграм,telegram,сообщение,чат,отправь в тг,дайджест"
                ),
                SkillEntity(
                    id = "mcp_cloud_vault",
                    name = "MCP Облачные хранилища",
                    description = "Безопасное взаимодействие с файлами, Google Drive, S3 и локальными документами через протокол MCP в реальном времени.",
                    category = "MCP",
                    icon = "cloud_sync",
                    isInstalled = true,
                    isEnabled = true,
                    version = "1.2.0",
                    author = "Model Context Protocol",
                    triggerWords = "диск,облако,файл,хранилище,документ,mcp storage"
                ),
                SkillEntity(
                    id = "voice_executive",
                    name = "Голосовой ассистент & Брифинг",
                    description = "Обработка естественной речи в реальном времени (NLP), озвучивание ответов и утренний голосовой брифинг.",
                    category = "SYSTEM",
                    icon = "mic",
                    isInstalled = true,
                    isEnabled = true,
                    version = "2.0.0",
                    author = "Hermes Audio",
                    triggerWords = "голос,озвучь,прочитай,брифинг,статус дня"
                )
            )
            db.skillDao().insertAll(defaultSkills)

            // Initial Calendar Events (Google & Outlook)
            val now = System.currentTimeMillis()
            val hourMs = 3600000L
            val dayMs = 86400000L

            val sampleEvents = listOf(
                CalendarEventEntity(
                    title = "Синхронизация по архитектуре MCP",
                    description = "Обсуждение интеграции Model Context Protocol коннекторов и протокола безопасности",
                    startTimeMillis = now + 2 * hourMs,
                    endTimeMillis = now + 3 * hourMs,
                    location = "Google Meet",
                    provider = "GOOGLE",
                    status = "CONFIRMED",
                    reminderMinutesBefore = 15,
                    attendees = "lead@hermes.ai, dev@company.com"
                ),
                CalendarEventEntity(
                    title = "Планирование квартального спринта",
                    description = "Обзор бэклога задач, назначение приоритетов и вех",
                    startTimeMillis = now + dayMs + (4 * hourMs),
                    endTimeMillis = now + dayMs + (5 * hourMs),
                    location = "Microsoft Teams Room A",
                    provider = "OUTLOOK",
                    status = "CONFIRMED",
                    reminderMinutesBefore = 30,
                    attendees = "pm@enterprise.com, team@outlook.com"
                ),
                CalendarEventEntity(
                    title = "Обзор автономных ИИ-навыков",
                    description = "Презентация новых навыков управления почтой и автономных цепочек",
                    startTimeMillis = now + (2 * dayMs) + (1 * hourMs),
                    endTimeMillis = now + (2 * dayMs) + (2 * hourMs),
                    location = "Online",
                    provider = "GOOGLE",
                    status = "CONFIRMED",
                    reminderMinutesBefore = 10,
                    attendees = "ceo@hermes.ai"
                )
            )
            for (event in sampleEvents) {
                db.calendarEventDao().insertEvent(event)
            }

            // Initial Notes
            val sampleNotes = listOf(
                NoteEntity(
                    title = "План автоматизации недели",
                    content = "1. Настроить MCP коннектор к Telegram для срочных алертов.\n2. Синхронизировать встречи Google и Outlook.\n3. Включить голосовой брифинг в 09:00.",
                    tags = "Планы,Автоматизация,MCP",
                    isPinned = true,
                    actionItems = "Подключить Telegram Bot,Проверить созвоны в Outlook"
                ),
                NoteEntity(
                    title = "Параметры безопасности MCP",
                    content = "Все внешние запросы должны фильтроваться встроенной локальной моделью для предотвращения утечки конфиденциальных данных.",
                    tags = "Безопасность,Приватность",
                    isPinned = false,
                    actionItems = "Аудит прав доступа"
                )
            )
            for (note in sampleNotes) {
                db.noteDao().insertNote(note)
            }

            // Initial MCP Servers
            val sampleMcpServers = listOf(
                McpServerEntity(
                    id = "mcp_google_calendar",
                    name = "Google Calendar MCP",
                    description = "Предоставляет доступ к созданию, чтению и синхронизации событий Google Calendar через JSON-RPC 2.0.",
                    transportType = "SSE",
                    endpointUrl = "https://mcp.hermes.local/v1/google-cal",
                    status = "CONNECTED",
                    toolCount = 4,
                    exposedTools = "[\"create_event\", \"list_events\", \"update_event\", \"delete_event\"]"
                ),
                McpServerEntity(
                    id = "mcp_outlook_calendar",
                    name = "Microsoft Outlook MCP",
                    description = "Интеграция с Microsoft Graph API для управления календарями Outlook и встречами Teams.",
                    transportType = "HTTP_REST",
                    endpointUrl = "https://mcp.hermes.local/v1/ms-graph-cal",
                    status = "CONNECTED",
                    toolCount = 3,
                    exposedTools = "[\"outlook_add_meeting\", \"outlook_list_calendar\", \"outlook_cancel_event\"]"
                ),
                McpServerEntity(
                    id = "mcp_telegram_bot",
                    name = "Telegram Dispatcher MCP",
                    description = "Мгновенная доставка уведомлений, дайджестов задач и срочных напоминаний в Telegram чаты.",
                    transportType = "STDIO",
                    endpointUrl = "telegram-bot-mcp://runtime",
                    status = "CONNECTED",
                    toolCount = 2,
                    exposedTools = "[\"send_telegram_alert\", \"broadcast_digest\"]"
                ),
                McpServerEntity(
                    id = "mcp_web_search",
                    name = "Live Web & DuckDuckGo MCP",
                    description = "Безопасный поиск актуальной информации в реальном времени без отслеживания пользователей.",
                    transportType = "SSE",
                    endpointUrl = "https://mcp.hermes.local/v1/web-search",
                    status = "CONNECTED",
                    toolCount = 2,
                    exposedTools = "[\"search_web\", \"extract_page_content\"]"
                ),
                McpServerEntity(
                    id = "mcp_cloud_vault",
                    name = "Cloud Storage Vault MCP",
                    description = "Шифрованное облачное хранилище документов, резервных копий и заметок с локальным кэшированием.",
                    transportType = "EMBEDDED",
                    endpointUrl = "vault://local-secure-store",
                    status = "CONNECTED",
                    toolCount = 3,
                    exposedTools = "[\"read_file\", \"write_file\", \"sync_cloud\"]"
                )
            )
            db.mcpDao().insertAllServers(sampleMcpServers)

            // Initial Welcome Message
            db.agentMessageDao().insertMessage(
                AgentMessageEntity(
                    role = "ASSISTANT",
                    content = "Привет! Я Hermes — твой персональный автономный ИИ-агент.\n\n" +
                            "• Я автоматизирую задачи и управляю расписанием.\n" +
                            "• Поддерживаю голосовые команды на естественном языке.\n" +
                            "• Полностью интегрирован с Google Calendar и Outlook Calendar.\n" +
                            "• Использую безопасные MCP-коннекторы и локальные ИИ-модели для 100% приватности.\n\n" +
                            "Нажми на микрофон или введи команду, например: «Запланируй встречу в Google Calendar завтра в 15:00» или «Сделай утренний брифинг».",
                    reasoningTrace = "Инициализация агента завершена. Активны 8 навыков и 5 MCP-коннекторов. Режим приватности: локальная обработка данных активна."
                )
            )
        }
    }
}
