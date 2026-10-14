package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import com.example.service.AgentTaskCategory
import com.example.service.BatteryGovernorManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hermes Agent", appName)
  }

  @Test
  fun `battery governor suspends LLM and MCP but protects calendar alerts in low power`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val governor = BatteryGovernorManager(context)

    // Initially in normal state
    governor.setSimulatedLowPower(false)
    assertTrue(governor.isTaskExecutionAllowed(AgentTaskCategory.LOCAL_LLM_HEAVY_INFERENCE))
    assertTrue(governor.isTaskExecutionAllowed(AgentTaskCategory.MCP_BACKGROUND_SYNC))
    assertTrue(governor.isTaskExecutionAllowed(AgentTaskCategory.URGENT_CALENDAR_ALERT))

    // Enable low power mode
    governor.setSimulatedLowPower(true)
    assertTrue("Low power mode must be active", governor.batteryState.value.isSuspended)
    assertFalse("Local LLM heavy inference must be suspended", governor.isTaskExecutionAllowed(AgentTaskCategory.LOCAL_LLM_HEAVY_INFERENCE))
    assertFalse("MCP background sync must be suspended", governor.isTaskExecutionAllowed(AgentTaskCategory.MCP_BACKGROUND_SYNC))
    assertTrue("Urgent calendar alerts must be protected and allowed", governor.isTaskExecutionAllowed(AgentTaskCategory.URGENT_CALENDAR_ALERT))

    governor.destroy()
  }
}
