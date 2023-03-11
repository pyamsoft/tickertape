package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Stable
interface BaseDigViewState : UiViewState {
  val loadingState: StateFlow<LoadingState>
  val ticker: StateFlow<Ticker>

  @Stable
  @Immutable
  enum class LoadingState {
    NONE,
    LOADING,
    DONE
  }
}

@Stable
interface ChartDigViewState : BaseDigViewState {
  val range: StateFlow<StockChart.IntervalRange>
  val currentDate: StateFlow<LocalDateTime>
  val currentPrice: StateFlow<StockMoneyValue?>
  val openingPrice: StateFlow<StockMoneyValue?>
  val chartError: StateFlow<Throwable?>
}

@Stable
interface NewsDigViewState : BaseDigViewState {
  val news: StateFlow<List<StockNews>>
  val newsError: StateFlow<Throwable?>
}

@Stable
interface StatisticsDigViewState : BaseDigViewState {
  val statistics: StateFlow<KeyStatistics?>
  val statisticsError: StateFlow<Throwable?>
}

@Stable
interface RecommendationDigViewState : BaseDigViewState {
  val recommendations: StateFlow<List<Ticker>>
  val recommendationError: StateFlow<Throwable?>
}

@Stable
interface OptionsChainDigViewState : BaseDigViewState {
  val optionsChain: StateFlow<StockOptions?>
  val optionsError: StateFlow<Throwable?>
  val optionsSection: StateFlow<StockOptions.Contract.Type>
  val optionsExpirationDate: StateFlow<LocalDate?>
}

@Stable
interface PriceAlertDigViewState : BaseDigViewState {
  val priceAlerts: StateFlow<List<PriceAlert>>
}

@Stable
interface DigViewState :
    ChartDigViewState,
    NewsDigViewState,
    StatisticsDigViewState,
    RecommendationDigViewState,
    OptionsChainDigViewState,
    PriceAlertDigViewState

@Stable
abstract class MutableDigViewState
protected constructor(
    symbol: StockSymbol,
    clock: Clock,
) : DigViewState {
  final override val loadingState = MutableStateFlow(BaseDigViewState.LoadingState.NONE)

  final override val optionsChain = MutableStateFlow<StockOptions?>(null)
  final override val optionsError = MutableStateFlow<Throwable?>(null)
  final override val optionsSection = MutableStateFlow(StockOptions.Contract.Type.CALL)
  final override val optionsExpirationDate = MutableStateFlow<LocalDate?>(null)

  final override val statistics = MutableStateFlow<KeyStatistics?>(null)
  final override val statisticsError = MutableStateFlow<Throwable?>(null)

  final override val recommendations = MutableStateFlow(emptyList<Ticker>())
  final override val recommendationError = MutableStateFlow<Throwable?>(null)

  final override val news = MutableStateFlow(emptyList<StockNews>())
  final override val newsError = MutableStateFlow<Throwable?>(null)

  final override val chartError = MutableStateFlow<Throwable?>(null)
  final override val range = MutableStateFlow(StockChart.IntervalRange.ONE_DAY)
  final override val currentDate = MutableStateFlow<LocalDateTime>(LocalDateTime.now(clock))
  final override val currentPrice = MutableStateFlow<StockMoneyValue?>(null)
  final override val openingPrice = MutableStateFlow<StockMoneyValue?>(null)

  final override val priceAlerts = MutableStateFlow(emptyList<PriceAlert>())

  final override val ticker =
      MutableStateFlow(
          Ticker(
              symbol = symbol,
              quote = null,
              chart = null,
          ),
      )
}
