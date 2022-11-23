package com.pyamsoft.tickertape

import android.app.Activity
import androidx.compose.runtime.Composable
import com.pyamsoft.pydroid.ui.app.ComposeThemeProvider
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.tickertape.ui.TickerTapeTheme

internal object TickerTapeThemeProvider : ComposeThemeProvider {

  @Composable
  override fun Render(
      activity: Activity,
      themeProvider: ThemeProvider,
      content: @Composable () -> Unit
  ) {
    activity.TickerTapeTheme(
        themeProvider = themeProvider,
        content = content,
    )
  }
}
