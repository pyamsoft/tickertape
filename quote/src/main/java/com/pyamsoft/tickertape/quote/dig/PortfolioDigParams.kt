package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Stable
@JsonClass(generateAdapter = true)
data class PortfolioDigParams(
    val symbol: StockSymbol,
    val lookupSymbol: StockSymbol?,
    val currentPrice: StockMoneyValue? = null,

    /** Don't JSON parse */
    @Json(ignore = true) val holding: DbHolding? = null,
)
