package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
enum class WatchlistDigSections(
    val display: String,
) {
  CHART("Chart"),
  STATISTICS("Details"),
  PRICE_ALERTS("Price Alerts"),
  NEWS("News"),
  RECOMMENDATIONS("Recommendations"),
  OPTIONS_CHAIN("Options Chain"),
}
