package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass

@Stable
@JsonClass(generateAdapter = true)
data class SplitParams(
    val symbol: StockSymbol,
    val holdingId: DbHolding.Id,
    val existingSplitId: DbSplit.Id,
) {}
