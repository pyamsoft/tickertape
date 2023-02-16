package com.pyamsoft.tickertape.main

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
sealed class MainPage(val displayName: String) {

  @Stable @Immutable object Home : MainPage("Home")

  @Stable @Immutable object Portfolio : MainPage("Portfolio")

  @Stable @Immutable object Notifications : MainPage("Notifications")
}
