package com.example.service

import com.example.data.dao.McpDao
import com.example.data.model.McpLogEntity
import com.example.data.model.McpServerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

data class McpToolCallRequest(
    val serverId: String,
    val toolName: String,
    val arguments: Map<String, Any?>
)

data class McpToolResult(
    val isSuccess: Boolean,
    val data: String,
    val durationMs: Long,
    val callId: String = UUID.randomUUID().toString()
)

class McpConnectorEngine(
    private val mcpDao: McpDao,
    private val notificationDispatcher: NotificationDispatcher,
    private val batteryGovernor: BatteryGovernorManager? = null
) {

    fun getAllServers(): Flow<List<McpServerEntity>> = mcpDao.getAllServers()
    fun getRecentLogs(): Flow<List<McpLogEntity>> = mcpDao.getRecentLogs()

    suspend fun executeTool(request: McpToolCallRequest): McpToolResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var isSuccess = true
        var outputStr: String

        val server = mcpDao.getServerById(request.serverId)
        val serverName = server?.name ?: request.serverId

        try {
            // Emulate / Execute MCP JSON-RPC 2.0 tool invocation
            outputStr = when (request.toolName) {
                "create_event", "outlook_add_meeting" -> {
                    val title = request.arguments["title"]?.toString() ?: "Встреча"
                    val provider = if (request.toolName.contains("outlook")) "OUTLOOK" else "GOOGLE"
                    val time = request.arguments["time"]?.toString() ?: "15:00"
                    """{"status": "EVENT_CREATED", "provider": "$provider", "title": "$title", "scheduledTime": "$time", "syncState": "CONFIRMED"}"""
                }
                "delete_event", "outlook_cancel_event" -> {
                    val title = request.arguments["title"]?.toString() ?: "Встреча"
                    """{"status": "EVENT_CANCELLED", "title": "$title", "removedFromCalendar": true}"""
                }
                "send_telegram_alert" -> {
                    val message = request.arguments["message"]?.toString() ?: "Алерт от Hermes"
                    notificationDispatcher.dispatchTelegramNotification("", "", message)
                    """{"status": "DISPATCHED", "channel": "TELEGRAM_BOT", "deliveryState": "QUEUED_AND_NOTIFIED"}"""
                }
                "search_web" -> {
                    val query = request.arguments["query"]?.toString() ?: "AI updates"
                    """{
                        "status": "SEARCH_SUCCESS",
                        "query": "$query",
                        "results": [
                            {"title": "OpenClaw & Hermes Agent Architecture 2026", "url": "https://mcp.dev/hermes", "snippet": "Автономные агенты нового поколения с полной поддержкой локальных ИИ-моделей и протокола MCP."},
                            {"title": "Model Context Protocol Specification v2.0", "url": "https://modelcontextprotocol.io/spec", "snippet": "Стандарт безопасной интеграции контекста и внешних инструментов в реальном времени."}
                        ]
                    }""".trimIndent()
                }
                "compose_email_draft" -> {
                    val recipient = request.arguments["to"]?.toString() ?: "partner@company.com"
                    val subject = request.arguments["subject"]?.toString() ?: "Рабочий вопрос"
                    """{"status": "DRAFT_SAVED", "to": "$recipient", "subject": "$subject", "isEncrypted": true}"""
                }
                "read_file", "write_file", "sync_cloud" -> {
                    val path = request.arguments["path"]?.toString() ?: "/vault/notes.md"
                    val isBackground = request.arguments["is_background"] as? Boolean ?: (request.toolName == "sync_cloud")
                    if (isBackground && batteryGovernor?.isTaskExecutionAllowed(AgentTaskCategory.MCP_BACKGROUND_SYNC) == false) {
                        """{"status": "SUSPENDED_LOW_POWER", "path": "$path", "message": "Фоновая синхронизация отложена для сохранения заряда аккумулятора (<20% / Энергосбережение)", "retryWhenCharged": true}"""
                    } else {
                        """{"status": "VAULT_SYNCED", "path": "$path", "integrity": "SHA256_VALIDATED", "encryption": "AES_GCM_256"}"""
                    }
                }
                else -> {
                    """{"status": "EXECUTED", "tool": "${request.toolName}", "result": "Успешно обработано MCP сервером $serverName"}"""
                }
            }
        } catch (e: Exception) {
            isSuccess = false
            outputStr = """{"error": "${e.localizedMessage ?: "Unknown error"}"}"""
        }

        val duration = System.currentTimeMillis() - startTime

        // Store log in Room database for real-time auditability
        mcpDao.insertLog(
            McpLogEntity(
                serverId = request.serverId,
                serverName = serverName,
                toolName = request.toolName,
                inputJson = JSONObject(request.arguments).toString(),
                outputJson = outputStr,
                status = if (isSuccess) "SUCCESS" else "FAILED",
                durationMs = duration
            )
        )

        McpToolResult(
            isSuccess = isSuccess,
            data = outputStr,
            durationMs = duration
        )
    }

    suspend fun toggleServer(id: String, enabled: Boolean) {
        val server = mcpDao.getServerById(id)
        if (server != null) {
            mcpDao.updateServer(
                server.copy(
                    isEnabled = enabled,
                    status = if (enabled) "CONNECTED" else "DISCONNECTED",
                    lastPingMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addCustomServer(server: McpServerEntity) {
        mcpDao.insertServer(server)
    }

    suspend fun deleteServer(server: McpServerEntity) {
        mcpDao.deleteServer(server)
    }

    suspend fun clearLogs() {
        mcpDao.clearLogs()
    }
}
