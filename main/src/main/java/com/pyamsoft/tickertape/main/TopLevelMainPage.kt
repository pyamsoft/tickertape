package com.pyamsoft.tickertape.main

interface MainPage

sealed class TopLevelMainPage(val displayName: String) : MainPage {
  object Home : TopLevelMainPage("Home")

  object Watchlist : TopLevelMainPage("Watchlist")

  object Portfolio : TopLevelMainPage("Portfolio")

  object Notifications : TopLevelMainPage("Notifications")
}
