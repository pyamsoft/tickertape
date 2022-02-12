package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import javax.inject.Inject

interface PortfolioDigViewState : DigViewState {
  val equityType: EquityType
  val tradeSide: TradeSide

  val isLoading: Boolean
  val section: PortfolioDigSections

  val holding: DbHolding?
  val positions: List<DbPosition>
  val holdingError: Throwable?
  val positionsError: Throwable?
}

// Public for PortfolioDigViewModeler constructor
class MutablePortfolioDigViewState
@Inject
internal constructor(
    override val equityType: EquityType,
    override val tradeSide: TradeSide,
    symbol: StockSymbol,
) : MutableDigViewState(symbol), PortfolioDigViewState {
  override var isLoading by mutableStateOf(false)
  override var section by mutableStateOf(PortfolioDigSections.CHART)

  override var holding by mutableStateOf<DbHolding?>(null)
  override var positions by mutableStateOf(emptyList<DbPosition>())
  override var positionsError by mutableStateOf<Throwable?>(null)
  override var holdingError by mutableStateOf<Throwable?>(null)
}
