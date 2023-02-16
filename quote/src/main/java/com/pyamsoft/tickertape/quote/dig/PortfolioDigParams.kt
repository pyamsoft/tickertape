package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

@Stable
data class PortfolioDigParams(
    val symbol: StockSymbol,
    val lookupSymbol: StockSymbol?,
    val holdingId: DbHolding.Id,
    val holdingType: EquityType,
    val holdingSide: TradeSide,
    val currentPrice: StockMoneyValue? = null,
    val specialUniqueKey: String = IdGenerator.generate(),
) {
  constructor(
      symbol: StockSymbol,
      lookupSymbol: StockSymbol?,
      holding: DbHolding,
      currentPrice: StockMoneyValue? = null,
  ) : this(
      symbol,
      lookupSymbol,
      holding.id,
      holding.type,
      holding.side,
      currentPrice,
  )
}
