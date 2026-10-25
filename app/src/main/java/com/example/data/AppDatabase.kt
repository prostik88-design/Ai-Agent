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
        AgentTaskEntity::class,
        ProjectEntity::class,
        DirectChatMessageEntity::class,
        AutomationWorkflowEntity::class,
        WorkflowExecutionLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun skillDao(): SkillDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun noteDao(): NoteDao
    abstract fun mcpDao(): McpDao
    abstract fun agentMessageDao(): AgentMessageDao
    abstract fun agentTaskDao(): AgentTaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun directChatMessageDao(): DirectChatMessageDao
    abstract fun automationWorkflowDao(): AutomationWorkflowDao
    abstract fun workflowExecutionLogDao(): WorkflowExecutionLogDao

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
                    content = "Привет! Я Hermes — твой персональный ассистент автоматизации процессов (Personal Process Automation Assistant).\n\n" +
                            "• ⚡ Оркестрирую рабочие пайплайны, задачи и повторяющиеся рутины.\n" +
                            "• 📅 Полностью синхронизирован с Google Calendar и Outlook Calendar.\n" +
                            "• 🔌 Запускаю внешние действия через MCP-коннекторы (Telegram, Cloud Vault, Web).\n" +
                            "• 🔋 Вторичный оптимизатор питания контролирует энергопотребление и защищает календарь.\n\n" +
                            "Нажми на микрофон или введи команду, например: «Запусти утренний брифинг», «Запланируй встречу в Google Calendar завтра в 15:00» или перейди во вкладку «Проекты» для настройки пайплайнов.",
                    reasoningTrace = "Инициализация агента завершена. Активны 4 рабочих пайплайна, 8 навыков и 5 MCP-коннекторов. Специализация: Personal Process Automation Assistant."
                )
            )

            // Initial Sample Projects
            val sampleProjects = listOf(
                ProjectEntity(
                    title = "Автоматизация рабочего расписания",
                    description = "Интеграция корпоративного Outlook с личным Google Calendar, настройка Telegram алертов и авто-брифингов.",
                    category = "Автоматизация",
                    status = "IN_PROGRESS",
                    progressPercent = 65,
                    taskCount = 6,
                    completedTaskCount = 4,
                    tags = "Календарь,MCP,Telegram"
                ),
                ProjectEntity(
                    title = "Безопасное MCP хранилище документов",
                    description = "Развертывание локального файлового хранилища с шифрованием AES-GCM и синхронизацией через облачный коннектор.",
                    category = "Безопасность",
                    status = "PLANNING",
                    progressPercent = 25,
                    taskCount = 4,
                    completedTaskCount = 1,
                    tags = "Vault,Приватность,S3"
                ),
                ProjectEntity(
                    title = "Интеграция ИИ моделей и Google Auth",
                    description = "Настройка авторизации по API ключу и через Google Account с гибкой регулировкой температуры ответов.",
                    category = "ИИ Модели",
                    status = "IN_PROGRESS",
                    progressPercent = 80,
                    taskCount = 5,
                    completedTaskCount = 4,
                    tags = "Gemini,Firebase,Auth"
                )
            )
            db.projectDao().insertAllProjects(sampleProjects)

            // Initial Direct Chatbot Message
            db.directChatMessageDao().insertMessage(
                DirectChatMessageEntity(
                    role = "ASSISTANT",
                    content = "Здравствуйте! Я быстрый диалоговый чат-бот Hermes. Здесь нет агентских цепочек и вызовов внешних инструментов — мы можем быстро обсудить любые вопросы, составить текст, провести мозговой штурм или решить задачу."
                )
            )

            // Initial Automation Workflows (Pipelines)
            val sampleWorkflows = listOf(
                AutomationWorkflowEntity(
                    id = 1,
                    title = "Утренний брифинг & Расписание дня",
                    description = "Синхронизирует встречи из Google и Outlook календарей, извлекает задачи и формирует дайджест в Заметках с отправкой в Telegram.",
                    category = "CALENDAR",
                    triggerType = "TIME_SCHEDULE",
                    triggerValue = "Ежедневно в 09:00",
                    actionsSummary = "Google/Outlook ➔ Дайджест в Заметки ➔ Telegram",
                    actionsJson = "[\"Синхронизация Google & Outlook календарей\", \"Генерация утренней сводки встреч\", \"Создание заметки-брифинга\", \"Отправка краткого анонса в Telegram\"]",
                    isEnabled = true,
                    lastRunMillis = now - 3600000L * 2,
                    lastRunStatus = "SUCCESS",
                    executionCount = 14
                ),
                AutomationWorkflowEntity(
                    id = 2,
                    title = "Синхронизация рабочих заметок & MCP Vault",
                    description = "Автоматически сканирует новые заметки, выделяет назначенные действия (Action Items) и выполняет резервную выгрузку в защищенное MCP хранилище.",
                    category = "NOTES",
                    triggerType = "TIME_SCHEDULE",
                    triggerValue = "Каждые 3 часа",
                    actionsSummary = "Анализ заметок ➔ Action Items ➔ MCP Cloud Vault",
                    actionsJson = "[\"Поиск несохраненных заметок\", \"Извлечение чек-листа действий\", \"Шифрование и синхронизация в MCP Vault\"]",
                    isEnabled = true,
                    lastRunMillis = now - 3600000L * 1,
                    lastRunStatus = "SUCCESS",
                    executionCount = 28
                ),
                AutomationWorkflowEntity(
                    id = 3,
                    title = "Подготовка контекста к встречам",
                    description = "За 15 минут до любого запланированного события собирает историю переписки, связанные заметки и выводит уведомление с повесткой.",
                    category = "ROUTINE",
                    triggerType = "CALENDAR_EVENT",
                    triggerValue = "За 15 мин до начала события",
                    actionsSummary = "Поиск контекста встречи ➔ Синтез темы ➔ Push алерт",
                    actionsJson = "[\"Мониторинг ближайших событий календаря\", \"Поиск релевантных заметок по ключевым словам\", \"Отправка всплывающего уведомления с повесткой\"]",
                    isEnabled = true,
                    lastRunMillis = now - 3600000L * 4,
                    lastRunStatus = "SUCCESS",
                    executionCount = 9
                ),
                AutomationWorkflowEntity(
                    id = 4,
                    title = "Фоновый оптимизатор питания (Power Guard)",
                    description = "При падении заряда аккумулятора <= 20% или переходе в режим Power Saver безопасно приостанавливает тяжелые LLM-пайплайны, сохраняя все срочные алерты.",
                    category = "OPTIMIZER",
                    triggerType = "BATTERY_OPTIMIZER",
                    triggerValue = "Заряд батареи <= 20% или Power Saver",
                    actionsSummary = "Battery Monitor ➔ Пауза тяжелых вычислений ➔ Защита календаря",
                    actionsJson = "[\"Телеметрия заряда аккумулятора и состояния питания\", \"Приостановка фонового LLM рассуждения\", \"Приостановка фоновой синхронизации MCP\", \"Гарантированное сохранение срочных напоминаний\"]",
                    isEnabled = true,
                    lastRunMillis = now - 3600000L * 6,
                    lastRunStatus = "SUCCESS",
                    executionCount = 5
                ),
                AutomationWorkflowEntity(
                    id = 5,
                    title = "Вечерний обзор проектов и бэклога",
                    description = "Анализирует прогресс проектов, подводит итоги спринта и формирует черновик плана ключевых задач на следующий рабочий день.",
                    category = "ROUTINE",
                    triggerType = "TIME_SCHEDULE",
                    triggerValue = "Ежедневно в 19:30",
                    actionsSummary = "Анализ прогресса ➔ Расчет дедлайнов ➔ Черновик задач",
                    actionsJson = "[\"Сканирование активных проектов и выполненных задач\", \"Формирование прогресс-отчета\", \"Запись черновика плана в Заметки\"]",
                    isEnabled = false,
                    lastRunMillis = now - 86400000L,
                    lastRunStatus = "SUCCESS",
                    executionCount = 8
                )
            )
            db.automationWorkflowDao().insertAll(sampleWorkflows)

            // Initial Execution Logs
            val sampleLogs = listOf(
                WorkflowExecutionLogEntity(
                    workflowId = 1,
                    workflowTitle = "Утренний брифинг & Расписание дня",
                    executedAtMillis = now - 3600000L * 2,
                    status = "SUCCESS",
                    durationMs = 420L,
                    summary = "Синхронизировано 2 события Google и 1 Outlook. Сформирована утренняя заметка с 3 задачами.",
                    batteryStateInfo = "Батарея: 88% (Разрядка) • Режим: Полная мощность"
                ),
                WorkflowExecutionLogEntity(
                    workflowId = 2,
                    workflowTitle = "Синхронизация рабочих заметок & MCP Vault",
                    executedAtMillis = now - 3600000L * 1,
                    status = "SUCCESS",
                    durationMs = 280L,
                    summary = "Обновлено 2 заметки в локальном хранилище и синхронизировано через MCP Cloud Vault.",
                    batteryStateInfo = "Батарея: 86% (Разрядка) • Режим: Обычный"
                ),
                WorkflowExecutionLogEntity(
                    workflowId = 3,
                    workflowTitle = "Подготовка контекста к встречам",
                    executedAtMillis = now - 3600000L * 4,
                    status = "SUCCESS",
                    durationMs = 190L,
                    summary = "Подготовлена повестка для созвона «Синхронизация по архитектуре MCP». Уведомление доставлено.",
                    batteryStateInfo = "Батарея: 92% (Разрядка)"
                ),
                WorkflowExecutionLogEntity(
                    workflowId = 4,
                    workflowTitle = "Фоновый оптимизатор питания (Power Guard)",
                    executedAtMillis = now - 3600000L * 6,
                    status = "SUCCESS",
                    durationMs = 95L,
                    summary = "Проверка состояния питания: заряд 94%, режим Power Saver выключен. Ограничения не требуются.",
                    batteryStateInfo = "Батарея: 94% (Разрядка) • Энергосбережение не активно"
                )
            )
            for (log in sampleLogs) {
                db.workflowExecutionLogDao().insertLog(log)
            }
        }
    }
}
