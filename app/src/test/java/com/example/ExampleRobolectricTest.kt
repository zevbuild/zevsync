package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.cache.VectorClockComparison
import com.example.data.cache.VectorClockEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SyncBeam", appName)
  }

  @Test
  fun `test vector clock conflict detection`() {
    val engine = VectorClockEngine("node-local")
    
    // Concurrent edits
    val clockA = """{"node-local": 2, "node-peer": 1}"""
    val clockB = """{"node-local": 1, "node-peer": 3}"""
    
    val comparison = engine.compare(clockA, clockB)
    assertEquals(VectorClockComparison.CONCURRENT_CONFLICT, comparison)

    // Merged clock
    val merged = engine.merge(clockA, clockB)
    val parsed = engine.parseClock(merged)
    assertEquals(2L, parsed["node-local"])
    assertEquals(3L, parsed["node-peer"])
  }
}
