package com.pyamsoft.tickertape.quote.add

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
enum class TickerDestination {
  WATCHLIST,
  PORTFOLIO
}
