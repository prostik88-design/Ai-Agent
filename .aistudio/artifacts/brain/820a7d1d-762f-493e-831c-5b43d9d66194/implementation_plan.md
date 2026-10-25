# Hermes Agent: Personal Process Automation Assistant

Transitioning the core specialization and persona of Hermes Agent from a dedicated battery monitor to a **Personal Process Automation Assistant (Персональный ассистент автоматизации процессов)**. The agent focuses on orchestrating multi-step task pipelines, continuous calendar synchronization, proactive routines, and MCP action chaining, while retaining battery and low-power telemetry as a secondary background optimizer in Agent Settings.

---

### User Review & Decisions Integration

> [!IMPORTANT]
> The architectural design directly integrates the three user preferences confirmed during Phase 1:
> 1. **Primary Focus**: Task pipelines, calendar sync (Google & Outlook), and automated routines (e.g. morning briefings, post-meeting note summaries, Telegram/MCP alerts).
> 2. **UI Presentation**: Visual workflow cards with triggers, chained actions, execution toggles, and execution run history.
> 3. **Telemetry Treatment**: Battery and low-power telemetry are preserved as a secondary background optimizer in the **Agent Settings** submenu (`Настройки агента`), rather than dominating the primary persona.

---

### 1. Overview & New Agent Persona

- **Specialization**: **Personal Process Automation Assistant**
  - **Role**: Proactively manages daily work routines, orchestrates cross-service pipelines (Calendar ➔ Notes ➔ Telegram ➔ Web Search ➔ MCP tools), and schedules automated tasks based on triggers (time, calendar events, battery state, or on-demand voice/text commands).
  - **Autonomous Execution Engine**: Chains actions together in structured pipelines with clear visual states and auditable logs.
  - **Secondary Optimizer**: If the device enters low-power mode (system battery saver or <= 20%), the background execution engine automatically throttles heavy periodic syncs while ensuring critical calendar alerts and high-priority workflows run safely.

---

### 2. User Experience & Visual Design

#### A. Main Header & Status Indicator (`Components.kt`)
- Title: **HERMES • AUTOMATION CORE**
- Subtitle: `⚡ 3 активных пайплайна • Автономный режим` (or status of currently executing workflow).
- Badges:
  - Automation Pipelines count indicator chip (e.g., `3 РУТИНЫ АКТИВНЫ`).
  - Privacy indicator chip (`LOCAL AI` / `HYBRID`).
  - Compact battery status indicator (`85% ⚡` / `20% ⚠️`) with click-through or reference to Settings.

#### B. Projects & Workflows Screen (`ProjectsScreen.kt`)
- Tabbed or unified view: **"ПРОЕКТЫ"** and **"ЦЕПОЧКИ АВТОМАТИЗАЦИИ (WORKFLOWS)"**:
  - **Visual Workflow Card**:
    - **Header**: Workflow Name, Category icon, and Enable/Disable toggle switch.
    - **Trigger Section**: `[ ⏰ Триггер ]` (e.g., `Каждый день в 09:00`, `За 15 мин до встречи`, `По голосовой команде`).
    - **Pipeline Action Steps**: Visual flow diagram chips:
      `[ 📅 Проверить Google Календарь ] ➔ [ 📝 Сформировать бриф в Заметках ] ➔ [ ✈️ Отправить в Telegram ]`
    - **Footer**: Last executed timestamp (e.g., `Сегодня, 09:00`), Run status (`✅ Успешно`, `⏱️ 420 мс`), and "Запустить сейчас" (Run Now) quick button.
  - **Execution History Modal / Bottom Sheet**:
    - Detailed run history for each workflow (timestamp, duration in ms, steps completed, battery state during run, and output summary).
  - **Pre-configured Starter Automations**:
    1. *«Утренний брифинг & Планирование»*: Сканирует встречи на день из Google/Outlook и генерирует краткий список дел в Заметках.
    2. *«Синхронизация рабочих заметок с MCP»*: Периодическая проверка несохраненных заметок и отправка важных заметок во внешние каналы.
    3. *«Power Optimizer Guard»*: Автоматический перевод тяжелых пайплайнов в щадящий режим при заряде аккумулятора ниже 20%.
    4. *«Анализ предстоящих встреч»*: За 15 минут до начала календарного события готовит контекст и выводит уведомление.

#### C. Settings Screen (`SettingsScreen.kt`)
- In **Section 5: "Подменю: Настройки агента"**:
  - Clear heading: **«Вторичный оптимизатор питания и ресурсов (Power Optimizer)»**.
  - Battery telemetry card, low-power simulation switch, and task suspension matrix remain fully functional here as an agent governor setting.

---

### 3. Architecture & Technical Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Hermes NLP Agent Engine                         │
│   • Persona: Personal Process Automation Assistant                     │
│   • Multi-step Pipeline Planner & Workflow Dispatcher                  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
               ┌────────────────────┼────────────────────┐
               ▼                    ▼                    ▼
   ┌───────────────────────┐ ┌─────────────┐ ┌──────────────────────┐
   │ Automation Workflows  │ │  Calendar   │ │  MCP Tools & System  │
   │ & Routines Engine     │ │ Integration │ │ (Telegram, Notes,    │
   │ (Trigger ➔ Action)    │ │ (GCal/MSft) │ │  Battery Optimizer)  │
   └───────────┬───────────┘ └──────┬──────┘ └──────────┬───────────┘
               │                    │                   │
               ▼                    ▼                   ▼
   ┌────────────────────────────────────────────────────────────────┐
   │             Room Database & Execution Audit History            │
   │   • AutomationWorkflowEntity                                   │
   │   • WorkflowExecutionLogEntity                                 │
   └────────────────────────────────────────────────────────────────┘
```

#### New / Updated Data Entities:
1. `AutomationWorkflowEntity`:
   - `id: Long = 0` (Primary Key)
   - `title: String`
   - `description: String`
   - `triggerType: String` (`SCHEDULE`, `CALENDAR_EVENT`, `BATTERY_CHANGE`, `MANUAL`)
   - `triggerValue: String`
   - `actionsJson: String` (list of pipeline step definitions)
   - `isEnabled: Boolean = true`
   - `lastRunMillis: Long? = null`
   - `lastRunStatus: String = "IDLE"`
   - `executionCount: Int = 0`

2. `WorkflowExecutionLogEntity`:
   - `id: Long = 0` (Primary Key)
   - `workflowId: Long`
   - `workflowTitle: String`
   - `executedAt: Long`
   - `status: String` (`SUCCESS`, `WARNING`, `FAILED`, `SKIPPED_LOW_POWER`)
   - `summary: String`
   - `durationMs: Long`

3. Database & DAOs:
   - `AutomationWorkflowDao` (queries for active workflows, toggling enabled state, recording runs).
   - `WorkflowExecutionLogDao` (inserting execution logs, retrieving history per workflow).
   - Update `AppDatabase` with pre-populated starter automations.

#### NLP Agent Engine Updates (`NlpAgentEngine.kt`):
- System prompt & persona updated:
  - From: `"Hermes Battery Usage Monitor & Low-Power Background Service Manager"`
  - To: `"Hermes Personal Process Automation Assistant (Персональный ассистент автоматизации процессов и рабочих пайплайнов)"`
- Recognizes automation intents:
  - `AUTOMATION_RUN`: Запуск пайплайна по названию или ID.
  - `AUTOMATION_CREATE`: Создание новой рутины автоматизации через диалог или голос.
  - `AUTOMATION_STATUS`: Запрос статуса активных процессов и истории их выполнения.
- Connects execution to real data (triggering notes creation, calendar queries, Telegram alerts).

---

### 4. Step-by-Step Implementation Sequence

1. **Step 1: Data Models & Room Schema**
   - Create `com.example.data.model.AutomationWorkflowEntity` and `WorkflowExecutionLogEntity`.
   - Create `com.example.data.dao.AutomationWorkflowDao` and `WorkflowExecutionLogDao`.
   - Register new entities and DAOs in `AppDatabase.kt` and bump database version or provide fallback migration with starter automation data.

2. **Step 2: Service Layer & NLP Engine**
   - Update `NlpAgentEngine.kt` persona and reasoning logs to reflect Personal Process Automation.
   - Add pipeline execution runner logic that processes steps (e.g. checking calendars, writing notes, notifying via Telegram/system notification).
   - Maintain battery governor as an execution constraint: if low power is active, heavy background sync steps are safely deferred.

3. **Step 3: ViewModel Integration (`HermesAgentViewModel.kt`)**
   - Expose `automationWorkflows: StateFlow<List<AutomationWorkflowEntity>>` and `executionLogs: StateFlow<List<WorkflowExecutionLogEntity>>`.
   - Add methods: `toggleWorkflow(id)`, `executeWorkflow(id)`, `createWorkflow(...)`, `deleteWorkflow(id)`.

4. **Step 4: UI Updates**
   - **`Components.kt`**: Update `AgentHeader` title, subtitle, and badges to highlight active automation routines and process status.
   - **`ProjectsScreen.kt`**: Implement visual workflow cards with trigger chips, action sequence arrows, "Запустить" button, and execution history bottom sheet.
   - **`SettingsScreen.kt`**: Clarify that battery telemetry in the agent submenu operates as a secondary background optimizer.

5. **Step 5: Compilation & Verification**
   - Run `compile_applet` to verify zero compiler errors or missing symbols.
   - Ensure UI previews and tests compile smoothly.
