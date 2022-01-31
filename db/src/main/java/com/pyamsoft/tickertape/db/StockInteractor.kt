package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockQuote

@CheckResult
suspend fun StockInteractor.getWatchListQuotes(
    force: Boolean,
    dao: SymbolQueryDao,
): List<StockQuote> {
  val watchList = dao.query(force).map { it.symbol() }
  return this.getQuotes(
      force,
      watchList,
  )
}
