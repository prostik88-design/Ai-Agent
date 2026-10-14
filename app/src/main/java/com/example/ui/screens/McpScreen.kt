package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.McpLogEntity
import com.example.data.model.McpServerEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun McpScreen(
    servers: List<McpServerEntity>,
    logs: List<McpLogEntity>,
    onToggleServer: (String, Boolean) -> Unit,
    onTestTool: (McpServerEntity, String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Servers, 1 = Live Audit Logs

    val activeCount = servers.count { it.isEnabled }
    val totalTools = servers.filter { it.isEnabled }.sumOf { it.toolCount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgentVoidDark)
            .padding(top = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MCP КОННЕКТОРЫ (MODEL CONTEXT PROTOCOL)",
                    color = AgentTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Активно $activeCount серверов • $totalTools доступных инструментов",
                    color = AgentEmerald,
                    fontSize = 11.sp
                )
            }
        }

        // Protocol Info Banner
        Surface(
            color = AgentSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AgentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MCP изолирует доступ к файлам, календарям и облакам. Все вызовы логируются и выполняются по строгому протоколу JSON-RPC 2.0.",
                    color = AgentTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AgentVoidDark,
            contentColor = AgentCyan,
            divider = { Divider(color = AgentCardStrokeDark) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Серверы (${servers.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Терминал / Аудит (${logs.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // Servers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                items(servers, key = { it.id }) { server ->
                    McpServerCard(
                        server = server,
                        onToggle = { enabled -> onToggleServer(server.id, enabled) },
                        onTestTool = { tool -> onTestTool(server, tool) }
                    )
                }
            }
        } else {
            // Live Audit Logs
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ЖУРНАЛ ВЫЗОВОВ JSON-RPC 2.0",
                        color = AgentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    TextButton(onClick = onClearLogs) {
                        Text("Очистить журнал", color = AgentTextTertiary, fontSize = 11.sp)
                    }
                }

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Логи пока пусты. Выполните команду агенту или нажмите «Тест» на одном из серверов.",
                            color = AgentTextTertiary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            McpLogCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun McpServerCard(
    server: McpServerEntity,
    onToggle: (Boolean) -> Unit,
    onTestTool: (String) -> Unit
) {
    val transportColor = when (server.transportType) {
        "SSE" -> AgentCyan
        "STDIO" -> AgentAmber
        "HTTP_REST" -> AgentEmerald
        else -> AgentViolet
    }

    Surface(
        color = AgentSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (server.isEnabled) AgentEmerald.copy(alpha = 0.4f) else AgentCardStrokeDark,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (server.isEnabled) AgentEmerald else AgentTextTertiary,
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Column {
                        Text(
                            text = server.name,
                            color = AgentTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = server.endpointUrl,
                            color = AgentTextTertiary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Switch(
                    checked = server.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AgentVoidDark,
                        checkedTrackColor = AgentEmerald,
                        uncheckedThumbColor = AgentTextTertiary,
                        uncheckedTrackColor = AgentSurfaceVariantDark
                    ),
                    modifier = Modifier.testTag("toggle_mcp_${server.id}")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = server.description,
                color = AgentTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = transportColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "MCP ${server.transportType}",
                        color = transportColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = AgentSurfaceVariantDark,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${server.toolCount} Tools",
                        color = AgentTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Button(
                    onClick = {
                        val firstTool = if (server.id.contains("google")) "create_event"
                        else if (server.id.contains("outlook")) "outlook_add_meeting"
                        else if (server.id.contains("telegram")) "send_telegram_alert"
                        else if (server.id.contains("search")) "search_web"
                        else "sync_cloud"
                        onTestTool(firstTool)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgentCyanDark.copy(alpha = 0.3f),
                        contentColor = AgentCyan
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("test_mcp_${server.id}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Тест Tool", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun McpLogCard(log: McpLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Surface(
        color = AgentVoidDark,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormat.format(Date(log.timestamp)),
                        color = AgentTextTertiary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.serverName,
                        color = AgentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " -> ${log.toolName}()",
                        color = AgentAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = if (log.status == "SUCCESS") AgentEmerald.copy(alpha = 0.15f) else AgentRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${log.durationMs}ms",
                        color = if (log.status == "SUCCESS") AgentEmerald else AgentRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.outputJson,
                color = AgentTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 3
            )
        }
    }
}
