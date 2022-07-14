package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import javax.inject.Inject
import javax.inject.Named

interface PortfolioDigViewState : DigViewState {
  val tradeSide: TradeSide

  val isLoading: Boolean
  val section: PortfolioDigSections

  val stockSplits: List<DbSplit>
  val stockSplitError: Throwable?

  val holding: DbHolding?
  val holdingError: Throwable?

  val positions: List<DbPosition>
  val positionsError: Throwable?
}

// Public for PortfolioDigViewModeler constructor
class MutablePortfolioDigViewState
@Inject
internal constructor(
    equityType: EquityType,
    @Named("lookup") lookupSymbol: StockSymbol?,
    symbol: StockSymbol,
    override val tradeSide: TradeSide,
) : MutableDigViewState(symbol, lookupSymbol, equityType), PortfolioDigViewState {
  override var isLoading by mutableStateOf(false)
  override var section by mutableStateOf(PortfolioDigSections.CHART)

  override var stockSplits by mutableStateOf(emptyList<DbSplit>())
  override var stockSplitError by mutableStateOf<Throwable?>(null)

  override var holding by mutableStateOf<DbHolding?>(null)
  override var holdingError by mutableStateOf<Throwable?>(null)

  override var positions by mutableStateOf(emptyList<DbPosition>())
  override var positionsError by mutableStateOf<Throwable?>(null)
}
