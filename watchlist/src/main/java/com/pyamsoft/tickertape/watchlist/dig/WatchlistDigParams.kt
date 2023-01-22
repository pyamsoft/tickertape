package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol

// TODO needs parcelize
@Stable
data class WatchlistDigParams(
    val symbol: StockSymbol,
    val lookupSymbol: StockSymbol,
    val equityType: EquityType,
)
