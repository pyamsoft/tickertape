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
import javax.inject.Inject

interface PortfolioDigViewState : DigViewState {
  val equityType: EquityType

  val isLoading: Boolean
  val section: PortfolioDigSections

  val holding: DbHolding?
  val positions: List<DbPosition>

  val chartError: Throwable?
  val holdingError: Throwable?
  val positionsError: Throwable?
}

// Public for PortfolioDigViewModeler constructor
class MutablePortfolioDigViewState
@Inject
internal constructor(
    override val equityType: EquityType,
    symbol: StockSymbol,
) : MutableDigViewState(symbol), PortfolioDigViewState {
  override var isLoading by mutableStateOf(false)
  override var section by mutableStateOf(PortfolioDigSections.POSITIONS)
  override var holding by mutableStateOf<DbHolding?>(null)
  override var positions by mutableStateOf(emptyList<DbPosition>())
  override var chartError by mutableStateOf<Throwable?>(null)
  override var positionsError by mutableStateOf<Throwable?>(null)
  override var holdingError by mutableStateOf<Throwable?>(null)
}
