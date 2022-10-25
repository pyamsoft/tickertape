package com.pyamsoft.tickertape.main

interface MainPage

sealed class TopLevelMainPage(
    val displayName: String,
    val showFab: Boolean,
) : MainPage {
  object Home :
      TopLevelMainPage(
          "Home",
          showFab = false,
      )

  object Watchlist :
      TopLevelMainPage(
          "Watchlist",
          showFab = true,
      )

  object Portfolio :
      TopLevelMainPage(
          "Portfolio",
          showFab = true,
      )
}
