package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // CALENDAR, EMAIL, NOTES, SEARCH, MESSENGER, MCP, SYSTEM
    val icon: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val version: String = "1.0.0",
    val author: String = "Hermes Community",
    val configJson: String = "{}",
    val permissions: String = "",
    val triggerWords: String = "" // comma separated keywords for auto-invocation
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String = "",
    val provider: String, // "GOOGLE", "OUTLOOK", "LOCAL"
    val status: String = "CONFIRMED", // CONFIRMED, TENTATIVE, CANCELLED
    val reminderMinutesBefore: Int = 15,
    val isNotified: Boolean = false,
    val attendees: String = "" // comma separated
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val actionItems: String = "" // json or comma separated
)

@Entity(tableName = "mcp_servers")
data class McpServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val transportType: String, // "STDIO", "SSE", "HTTP_REST", "EMBEDDED"
    val endpointUrl: String,
    val status: String = "CONNECTED", // CONNECTED, DISCONNECTED, ERROR
    val toolCount: Int = 3,
    val exposedTools: String = "[]", // JSON array of tool names
    val lastPingMillis: Long = System.currentTimeMillis(),
    val authToken: String = "",
    val isEnabled: Boolean = true
)

@Entity(tableName = "mcp_logs")
data class McpLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: String,
    val serverName: String,
    val toolName: String,
    val inputJson: String,
    val outputJson: String,
    val status: String, // "SUCCESS", "FAILED"
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_messages")
data class AgentMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "USER", "ASSISTANT", "SYSTEM", "TOOL"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val reasoningTrace: String = "", // Hermes Chain-of-Thought
    val toolCallsJson: String = "", // Actions taken
    val audioDurationSec: Int = 0
)

@Entity(tableName = "agent_tasks")
data class AgentTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val prompt: String,
    val scheduleCronOrTime: String,
    val skillKey: String,
    val isAutonomous: Boolean = true,
    val isActive: Boolean = true,
    val lastRunMillis: Long = 0L,
    val nextRunMillis: Long = 0L,
    val lastStatus: String = "IDLE"
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String = "Разработка",
    val status: String = "IN_PROGRESS", // "PLANNING", "IN_PROGRESS", "COMPLETED"
    val progressPercent: Int = 35,
    val deadlineMillis: Long = System.currentTimeMillis() + 86400000L * 7,
    val taskCount: Int = 5,
    val completedTaskCount: Int = 2,
    val tags: String = "AI,MCP,Automation",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_chat_messages")
data class DirectChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "USER", "ASSISTANT"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
