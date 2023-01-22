package com.pyamsoft.tickertape.quote.screen

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass

@Stable
@JsonClass(generateAdapter = true)
data class WatchlistDigParams(
    val uniqueId: String,
    val symbol: StockSymbol,
    val lookupSymbol: StockSymbol,
    val equityType: EquityType,
)
