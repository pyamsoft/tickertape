package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface WatchlistDigViewState : DigViewState {
  val section: StateFlow<WatchlistDigSections>

  val watchlistStatus: StateFlow<WatchlistStatus>
  val isInWatchlistError: StateFlow<Throwable?>

  val digRecommendation: StateFlow<Ticker?>

  @Stable
  @Immutable
  enum class WatchlistStatus {
    NONE,
    IN_LIST,
    NOT_IN_LIST
  }
}

@Stable
class MutableWatchlistDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
) : MutableDigViewState(symbol), WatchlistDigViewState {

  override val section = MutableStateFlow(WatchlistDigSections.CHART)

  override val watchlistStatus = MutableStateFlow(WatchlistDigViewState.WatchlistStatus.NONE)
  override val isInWatchlistError = MutableStateFlow<Throwable?>(null)

  override val digRecommendation = MutableStateFlow<Ticker?>(null)
}
