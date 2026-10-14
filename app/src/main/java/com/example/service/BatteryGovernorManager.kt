package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AgentTaskCategory {
    LOCAL_LLM_HEAVY_INFERENCE,
    MCP_BACKGROUND_SYNC,
    URGENT_CALENDAR_ALERT,
    USER_INTERACTIVE_COMMAND
}

data class BatteryState(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isSuspended: Boolean = false,
    val isSimulatedLowPower: Boolean = false,
    val health: String = "Good",
    val temperatureCelsius: Float = 25.0f
)

class BatteryGovernorManager(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _batteryState = MutableStateFlow(BatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryStatus(intent)
        }
    }

    init {
        registerReceivers()
        // Query initial sticky intent
        val initialIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateBatteryStatus(initialIntent)
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            }
        }
        try {
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            // Fallback for restricted environments
        }
    }

    private fun updateBatteryStatus(intent: Intent?) {
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val percent = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 85

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250) ?: 250) / 10f

        val isSystemPowerSave = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager?.isPowerSaveMode ?: false
        } else {
            false
        }

        val currentState = _batteryState.value
        val effectiveLowPower = if (currentState.isSimulatedLowPower) {
            true
        } else {
            // Rule: System Power Saver mode OR battery <= 20% (while not plugged into charger)
            isSystemPowerSave || (percent <= 20 && !isCharging)
        }

        _batteryState.value = currentState.copy(
            levelPercent = if (currentState.isSimulatedLowPower) 18 else percent,
            isCharging = if (currentState.isSimulatedLowPower) false else isCharging,
            isPowerSaveMode = if (currentState.isSimulatedLowPower) true else isSystemPowerSave,
            isSuspended = effectiveLowPower,
            temperatureCelsius = temp
        )
    }

    fun setSimulatedLowPower(enabled: Boolean) {
        val current = _batteryState.value
        _batteryState.value = current.copy(
            isSimulatedLowPower = enabled,
            levelPercent = if (enabled) 18 else 85,
            isCharging = if (enabled) false else current.isCharging,
            isPowerSaveMode = if (enabled) true else current.isPowerSaveMode,
            isSuspended = if (enabled) true else (current.isPowerSaveMode || (current.levelPercent <= 20 && !current.isCharging))
        )
    }

    /**
     * Governs task execution based on energy status:
     * - URGENT_CALENDAR_ALERT: Always allowed!
     * - USER_INTERACTIVE_COMMAND: Allowed in lightweight mode
     * - LOCAL_LLM_HEAVY_INFERENCE & MCP_BACKGROUND_SYNC: Suspended when in low power mode!
     */
    fun isTaskExecutionAllowed(taskCategory: AgentTaskCategory): Boolean {
        val state = _batteryState.value
        if (!state.isSuspended) return true

        return when (taskCategory) {
            AgentTaskCategory.URGENT_CALENDAR_ALERT -> true // Whitelisted per user decision!
            AgentTaskCategory.USER_INTERACTIVE_COMMAND -> true // Allow direct user conversation with power warning
            AgentTaskCategory.LOCAL_LLM_HEAVY_INFERENCE -> false // Suspended!
            AgentTaskCategory.MCP_BACKGROUND_SYNC -> false // Suspended!
        }
    }

    fun destroy() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}
