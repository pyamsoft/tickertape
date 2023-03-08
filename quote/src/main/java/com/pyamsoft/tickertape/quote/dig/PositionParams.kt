package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass

@Stable
@JsonClass(generateAdapter = true)
data class PositionParams(
    val symbol: StockSymbol,
    val holdingId: DbHolding.Id,
    val holdingType: EquityType,
    val existingPositionId: DbPosition.Id,
)
