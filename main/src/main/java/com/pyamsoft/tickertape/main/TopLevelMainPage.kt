package com.pyamsoft.tickertape.main

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

interface MainPage

@Stable
@Immutable
sealed class TopLevelMainPage(val displayName: String) : MainPage {

  @Stable @Immutable object Home : TopLevelMainPage("Home")

  @Stable @Immutable object Portfolio : TopLevelMainPage("Portfolio")

  @Stable @Immutable object Notifications : TopLevelMainPage("Notifications")
}
