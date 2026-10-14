# Hermes Agent: Battery Usage Monitor & Low-Power Background Service Manager

An intelligent energy-aware background service manager for Hermes Agent that actively monitors battery level and Android system power-save states, automatically suspending heavy on-device LLM processing and background MCP synchronization tasks during low power conditions while preserving critical calendar meeting alerts.

### User Review & Critical Decisions

> [!IMPORTANT]
> The architectural design directly integrates the three user preferences gathered during Phase 1:
> - **Selective Task Suspension**: Local LLM heavy inferences and background MCP sync tasks are suspended; critical calendar reminders and urgent meeting alerts are explicitly protected and kept active.
> - **Trigger Condition**: Low-power state triggers when Android OS reports `PowerManager.isPowerSaveMode == true` OR device battery level drops below 20% (and is not charging), with an interactive simulator switch for testing.
> - **UI Surface**: A dedicated Battery & Resource Telemetry Card with an active status banner, real-time battery diagnostics, and a list of suspended vs. protected background services.

---

### 1. Overview & Core Concept

- **What It Does**: The `BatteryManagerService` acts as the power governor for the Hermes Agent engine. It tracks battery percentage, charging state, thermal pressure, and OS power-saver mode via a broadcast receiver and `PowerManager` hooks. When entering low-power mode, the manager immediately dispatches suspension signals to the local NLP engine and MCP sync routines, reducing CPU cycles and background network wake-locks.
- **Target Audience / Persona**: Mobile professionals and autonomous agent users who rely on Hermes Agent throughout the day and need guarantee that on-device AI tasks will not exhaust device battery or interrupt critical schedule alerts.
- **Key Value**: Protects device battery longevity and eliminates unwanted background battery drain without compromising schedule reliability or meeting attendance.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. *Normal Operation Flow*: Battery card in the "Заметки & Приватность" (and Agent Overview) shows "Battery Healthy (85%) • Full Autonomous Mode". All MCP services and LLM tasks run without throttles.
  2. *Low-Power Trigger Flow*: Device battery drops to 19% or user enables system battery saver. An amber/cyan high-tech banner appears: `⚡ ЭНЕРГОСБЕРЕЖЕНИЕ АКТИВНО: Локальный LLM и синхронизация MCP приостановлены. Срочные уведомления календаря работают штатно.`
  3. *Simulation & Control Flow*: In the telemetry dashboard, users can toggle "Режим симуляции низкого заряда (Demo Mode)" to verify that suspension behavior triggers immediately and see the live state change without having to drain their device.
  4. *Recovery Flow*: Plugging in the charger or turning off battery saver automatically restores background jobs, resuming pending synchronizations and logging the recovery event to the MCP audit trail.

- **Visual Identity & Theme**:
  - Consistent with the Hermes cybernetic dark aesthetic (`#0A0E17` void black, glowing cyan `#00E5FF`, warning amber `#FFB300`, and emerald `#00E676`).
  - *Battery Telemetry Card*: High-contrast card with custom battery meter bar, percentage readout, charging bolt icon, power save mode badge, and service status chips (`LLM: ПРИОСТАНОВЛЕН`, `MCP Sync: ПАУЗА`, `Календарь: АКТИВЕН`).
  - *Status Banner*: Striking warning-tinted banner at the top of the chat and settings view when power-saving suspension is active.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Selective Suspension vs. Complete Agent Shutdown**
  - *Chosen Approach*: Allow urgent calendar alerts and notification alarms to proceed; suspend continuous NLP agent reasoning, repetitive MCP background fetches, and cloud sync loops.
  - *Why*: Ensures the user never misses an important meeting or appointment while eliminating 95%+ of CPU and background network wakeups.
  - *Alternatives Considered*: Complete termination of all background processes was rejected because missing calendar alerts undermines the primary executive assistant function.

- **Decision 2: Dual Triggering Logic (OS Power-Save + Battery Threshold < 20%)**
  - *Chosen Approach*: React both to Android `ACTION_POWER_SAVE_MODE_CHANGED` and `ACTION_BATTERY_CHANGED` (threshold <= 20% while discharging), with a manual testing override.
  - *Why*: Some Android OEM skins don't aggressively enable power-save mode until 15% or 5%, whereas heavy local LLM inference can cause rapid thermal and voltage drops if allowed to run freely at 19%.

- **Decision 3: Reactive StateFlow Governor vs. Heavy Background Android WorkManager**
  - *Chosen Approach*: In-memory reactive `BatteryGovernor` exposed through ViewModel as `StateFlow<BatteryState>`, bound to a BroadcastReceiver registered in `MainActivity` / Application scope.
  - *Why*: Instant UI reactivity, zero additional APK bloat, immediate suspension of ongoing coroutines in `NlpAgentEngine` and `McpConnectorEngine`.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

```
┌─────────────────────────────────────────────────────────────┐
│                       Android OS                            │
│  [BatteryManager] ─── [PowerManager] ─── [BroadcastReceiver]│
└──────────────────────────────┬──────────────────────────────┘
                               │ ACTION_BATTERY_CHANGED
                               │ ACTION_POWER_SAVE_MODE_CHANGED
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 BatteryGovernorManager                      │
│  - Tracks: level, isCharging, isPowerSave, isSimulated      │
│  - Evaluates: isLowPowerMode = isPowerSave || (lvl <= 20)   │
│  - Emits: StateFlow<BatteryStatus>                          │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
      Suspension Signal              Status Updates
               │                              │
               ▼                              ▼
┌──────────────────────────────┐ ┌───────────────────────────┐
│        Agent Engines         │ │   UI Telemetry Dashboard  │
│  • NlpAgentEngine            │ │  • Battery Telemetry Card │
│    [Local LLM Paused]        │ │  • Low-Power Alert Banner │
│  • McpConnectorEngine        │ │  • Task State Matrix:     │
│    [Background Sync Paused]  │ │    - LLM: Paused          │
│  • CalendarManager           │ │    - MCP Sync: Paused     │
│    [ALERTS PROTECTED & ON]   │ │    - Calendar: Active     │
└──────────────────────────────┘ └───────────────────────────┘
```

- **Core Components to Add / Enhance**:
  1. `com.example.service.BatteryGovernorManager`:
     - BroadcastReceiver monitoring `ACTION_BATTERY_CHANGED`, `ACTION_POWER_CONNECTED`, `ACTION_POWER_DISCONNECTED`, and `ACTION_POWER_SAVE_MODE_CHANGED`.
     - Exposes `batteryState: StateFlow<BatteryState>` (`levelPercent: Int`, `isCharging: Boolean`, `isPowerSaveMode: Boolean`, `isLowPowerSuspended: Boolean`, `simulatedLowPower: Boolean`).
     - Methods: `setSimulatedLowPower(enabled: Boolean)`.
  2. `com.example.service.NlpAgentEngine`:
     - Injects check before heavy background local inference: if `batteryGovernor.isLowPowerSuspended.value`, defers heavy background tasks and provides informative low-power notice while answering direct queries concisely.
  3. `com.example.service.McpConnectorEngine`:
     - Guards scheduled background sync tools (`sync_cloud`, heavy periodic web fetches): gracefully defers them during low-power mode, while allowing manual on-demand execution.
  4. `com.example.service.CalendarIntegrationManager`:
     - Explicitly whitelisted: all meeting alerts, upcoming schedule reminders, and intent dispatches remain fully functional.
  5. `com.example.ui.screens.NotesAndSettingsScreen` & `AgentChatScreen`:
     - Add `BatteryTelemetryCard` showing live percentage, charging status, power-save mode, task matrix, and simulation toggle.
     - Add `LowPowerStatusBar` at the top of the Agent interface when power-saving is engaged.
