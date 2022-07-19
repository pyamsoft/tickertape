package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

internal fun newTestHolding(symbol: StockSymbol): DbHolding {
  return object : DbHolding {
    override val id: DbHolding.Id = DbHolding.Id("TEST")
    override val symbol: StockSymbol = symbol
    override val type: EquityType = EquityType.STOCK
    override val side: TradeSide = TradeSide.BUY
  }
}
