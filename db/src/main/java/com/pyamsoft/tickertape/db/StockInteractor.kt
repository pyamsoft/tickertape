package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockQuote

@CheckResult
suspend fun StockInteractor.getWatchListQuotes(
    dao: SymbolQueryDao,
): List<StockQuote> {
  val watchList = dao.query().map { it.symbol }
  return this.getQuotes(watchList)
}
