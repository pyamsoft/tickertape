package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

internal fun newTestHolding(symbol: StockSymbol): DbHolding {
  return object : DbHolding {
    override fun id(): DbHolding.Id {
      return DbHolding.Id("TEST")
    }

    override fun symbol(): StockSymbol {
      return symbol
    }

    override fun realEquityType(): String {
      return "STOCK"
    }

    override fun type(): EquityType {
      return EquityType.STOCK
    }

    override fun side(): TradeSide {
      return TradeSide.BUY
    }
  }
}
