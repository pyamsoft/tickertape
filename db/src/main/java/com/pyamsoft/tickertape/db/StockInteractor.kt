package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockQuote

@CheckResult
suspend fun StockInteractor.getQuotesForHoldings(
    dao: HoldingQueryDao,
): List<StockQuote> {
  val watchList = dao.query().map { it.symbol }
  return this.getQuotes(watchList)
}
