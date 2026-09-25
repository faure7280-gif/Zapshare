package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZapShare", appName)
  }

  @Test
  fun `transfer item format bytes is correct`() {
    val sizeBytes = 104857600L // 100 MB
    val formatted = com.example.model.TransferItem.formatBytes(sizeBytes)
    assertEquals("100.0 MB", formatted)
  }
}
