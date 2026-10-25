Hermes Agent: Personal Process Automation Assistant

https://img.shields.io/badge/version-2.0.0-blue.svg
https://img.shields.io/badge/status-active-success.svg
https://img.shields.io/badge/license-MIT-green.svg
https://img.shields.io/badge/python-3.10%2B-blue.svg
https://img.shields.io/badge/MCP-compatible-purple.svg

Hermes Agent — персональный ассистент автоматизации процессов. Оркестратор многошаговых конвейеров задач, синхронизации календаря в реальном времени, проактивных рутин и цепочек MCP-действий — с телеметрией батареи и энергопотребления в роли вторичного фонового оптимизатора.

---

📖 О проекте

Hermes Agent прошёл фундаментальную трансформацию: если раньше его ядро было сфокусировано на мониторинге состояния батареи и энергопотребления устройства, то теперь основная специализация смещена в сторону персональной автоматизации процессов.

Батарейная телеметрия не исчезла — она перешла в разряд вторичного фонового оптимизатора, доступного в разделе Agent Settings → Power & Telemetry. Теперь Hermes использует эти данные, чтобы умно планировать выполнение ресурсоёмких пайплайнов, не мешая работе устройства и не разряжая его в неподходящий момент.

Ключевая идея

Hermes превращает разрозненные действия — проверку почты, парсинг событий, вызовы инструментов через MCP, уведомления — в единый управляемый конвейер, который запускается по расписанию, событию или проактивному триггеру.

---

✨ Основные возможности

🔗 Многошаговые конвейеры задач (Task Pipelines)

Описывайте сложные сценарии как последовательность связанных шагов с условиями, ветвлениями и обработкой ошибок. Hermes выполняет их атомарно или с сохранением состояния между шагами.

```yaml
pipeline: morning_briefing
trigger: { cron: "0 7 * * 1-5" }
steps:
  - id: fetch_calendar
    action: mcp.google_calendar.list_events
    params: { range: "today", max_results: 10 }
  - id: fetch_weather
    action: mcp.weather.current
    params: { location: "$profile.city" }
  - id: summarize
    action: llm.summarize
    inputs: [fetch_calendar.output, fetch_weather.output]
  - id: notify
    action: mcp.telegram.send_message
    params: { chat_id: "$profile.chat_id", text: "$summarize.output" }
```

📅 Непрерывная синхронизация календаря

Двусторонняя синхронизация с Google Calendar, Outlook и CalDAV. Hermes отслеживает изменения в реальном времени через webhooks и push-каналы, автоматически реагируя на переносы, отмены и новые события.

· Pull + push синхронизация с разрешением конфликтов
· Инкрементальные обновления (sync tokens)
· Реакции на изменения: уведомления, перепланирование пайплайнов, перенос напоминаний

⏰ Проактивные рутины

Hermes не ждёт, пока его попросят. Он анализирует паттерны и предлагает действия:

· «Завтра у вас 3 встречи подряд — собрать повестку?»
· «Еженедельный отчёт готов к отправке»
· «Найдено 5 задач без дедлайна — назначить приоритет?»

Рутины настраиваются в Agent Settings → Routines с гибкими триггерами: cron, событие календаря, состояние задачи, погода, местоположение.

🧩 Цепочки MCP-действий (MCP Action Chaining)

Полная поддержка Model Context Protocol для композиции инструментов от разных серверов в единый поток:

· Композиция выходов одного MCP-сервера как входов другого
· Трансформации между шагами (JSONPath, Jinja2)
· Условные переходы и retry-политики
· Изоляция прав доступа на уровне сервера

🔋 Фоновый оптимизатор питания

Унаследованный от прежней версии модуль телеметрии теперь работает в фоновом режиме и служит планировщику пайплайнов:

· Определяет «энергетические окна» для ресурсоёмких задач (LLM-вызовы, синхронизации)
· Учитывает низкий заряд батареи и режим энергосбережения ОС
· Автоматически откладывает некритичные рутины
· Настройка в Agent Settings → Power & Telemetry

---

🏗️ Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                     Hermes Agent Core                       │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Pipeline    │  │  Scheduler   │  │  MCP Gateway     │  │
│  │  Engine      │◄─┤  (cron/event)│─►│  (chaining)      │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │            │
│  ┌──────▼─────────────────▼────────────────────▼─────────┐ │
│  │           State Store (SQLite / Postgres)             │ │
│  └──────┬─────────────────────────────────────┬──────────┘ │
│         │                                     │            │
│  ┌──────▼──────────┐              ┌───────────▼─────────┐ │
│  │ Calendar Sync   │              │ Proactive Routines  │ │
│  │ (Google/OUT/CAL)│              │ (pattern engine)    │ │
│  └─────────────────┘              └─────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ Power & Telemetry (Background Optimizer)              │ │
│  │  — Energy windows  — Battery-aware scheduling         │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

🚀 Быстрый старт

Требования

· Python 3.10+
· SQLite (по умолчанию) или PostgreSQL 14+
· Доступ к MCP-серверам (Google Calendar, Telegram, Weather, любой совместимый)

Установка

```bash
# клонирование
git clone https://github.com/your-org/hermes-agent.git
cd hermes-agent

# установка зависимостей
pip install -e .

# инициализация
hermes init

# запуск агента
hermes run
```

Конфигурация

~/.hermes/config.yaml:

```yaml
agent:
  name: "Hermes"
  timezone: "Europe/Moscow"

calendar:
  providers:
    - type: google
      credentials: ~/.hermes/secrets/google.json
      sync_interval: 60s
    - type: caldav
      url: "https://cal.example.com/dav"
      username: !secret cal_user
      password: !secret cal_password

mcp:
  servers:
    - name: telegram
      command: ["mcp-telegram", "--token", "!secret tg_token"]
    - name: weather
      command: ["mcp-weather", "--api-key", "!secret wapi_key"]
    - name: gcal
      command: ["mcp-google-calendar"]

routines:
  enabled: true
  quiet_hours: "23:00-07:00"

power:
  # бывшая "главная" роль — теперь фоновый оптимизатор
  enabled: true
  mode: "balanced"       # off | balanced | aggressive
  defer_noncritical_below: 20  # % заряда, при котором откладывать некритичные задачи
  respect_os_saver: true
```

---

🧠 Примеры использования

Пример 1. Триаж утренней почты + календарь

```yaml
pipeline: morning_triage
trigger: { cron: "30 7 * * 1-5" }

steps:
  - id: mail
    action: mcp.gmail.list
    params: { query: "is:unread newer_than:1d", max: 20 }

  - id: classify
    action: llm.classify
    params:
      inputs: $mail.output
      labels: [urgent, meeting, info, ignore]

  - id: schedule_meetings
    action: mcp.google_calendar.create_event
    foreach: $classify.outputs[label=meeting]
    params:
      title: "$item.subject"
      attendees: $item.senders
      duration: 30m

  - id: digest
    action: mcp.telegram.send_message
    params:
      chat_id: $profile.chat_id
      text: |
        ☀️ Утро:
        • Срочных: {{ classify.count('urgent') }}
        • Встреч создано: {{ schedule_meetings.count }}
        • К прочтению позже: {{ classify.count('info') }}
```

Пример 2. Реакция на перенос встречи

```yaml
pipeline: on_event_moved
trigger:
  type: calendar_change
  provider: google
  event: updated
  filter: "old.start != new.start"

steps:
  - id: notify_team
    action: mcp.telegram.send_message
    params:
      chat_id: $profile.work_chat
      text: "📅 Встреча «$event.title» перенесена: $old.start → $new.start"

  - id: reshuffle_prep
    action: pipeline.run
    params:
      name: meeting_prep
      context: { event_id: "$event.id" }
      # выполнится заранее: за 1 час до нового времени
```

Пример 3. Проактивная рутина: «Пятничный отчёт»

```yaml
routine: weekly_report
schedule: { cron: "0 16 * * 5" }
requires_confirmation: true

steps:
  - id: pull_tasks
    action: mcp.notion.query
    params: { database: "tasks", filter: "completed_this_week" }

  - id: compose
    action: llm.compose
    params:
      template: "weekly_report.md"
      data: $pull_tasks.output

  - id: send
    action: mcp.gmail.send
    params:
      to: "$profile.manager_email"
      subject: "Еженедельный отчёт — {{ now('%d.%m.%Y') }}"
      body: $compose.output
```

---

⚙️ Agent Settings

Все параметры доступны как через CLI (hermes config), так и через Web UI (hermes ui → Agent Settings).

Раздел Что настраивается
General Имя агента, часовой пояс, язык, идентичность
Pipelines Реестр пайплайнов, дефолтные retry-политики, лимиты параллелизма
Calendar Sync Провайдеры, интервалы, разрешение конфликтов, webhooks
Routines Проактивные сценарии, quiet hours, каналы уведомлений
MCP Servers Подключения, права, цепочки действий
Power & Telemetry ⚡ Фоновый оптимизатор: режим, пороги батареи, OS power saver

💡 Power & Telemetry — это не основной модуль, а вспомогательный слой, который делает планировщик энерго-осознанным. Его можно полностью отключить (power.enabled: false), и Hermes продолжит работать как ассистент автоматизации.

---

🛠 CLI

```bash
hermes run                            # запуск демона
hermes pipeline list                  # список пайплайнов
hermes pipeline run morning_briefing  # ручной запуск
hermes pipeline logs <id>             # логи выполнения
hermes calendar sync --force          # принудительная синхронизация
hermes routine trigger weekly_report  # запуск рутины
hermes mcp test telegram              # проверка MCP-сервера
hermes power status                   # статус фонового оптимизатора
hermes ui                             # Web UI на http://localhost:8080
```

---

🔐 Безопасность

· Секреты хранятся через !secret резолверы (keyring / env / vault)
· MCP-серверы изолированы по принципу наименьших привилегий
· Все изменения календаря и внешние вызовы логируются в audit trail
· Рутины с requires_confirmation: true не выполняют деструктивных действий без явного подтверждения

---

🗺️ Roadmap

☑ Переход ядра на pipeline-движок
☑ Двусторонняя синхронизация календаря
☑ MCP action chaining
☑ Power-модуль в роли фонового оптимизатора
☐ Графовый редактор пайплайнов в Web UI
☐ Локальные LLM-провайдеры (Ollama, llama.cpp)
☐ Плагины рутин из marketplace
☐ Мультиагентная оркестрация (sub-agents)

---

🤝 Вклад

PR и issues приветствуются. Перед отправкой PR, пожалуйста:

```bash
pip install -e ".[dev]"
pre-commit install
pytest
```

Подробности — в CONTRIBUTING.md.

---

📄 Лицензия

MIT — см. LICENSE.

---

<p align="center">
  <b>Hermes Agent</b> — от монитора батареи к оркестратору вашей повседневности.
</p>

