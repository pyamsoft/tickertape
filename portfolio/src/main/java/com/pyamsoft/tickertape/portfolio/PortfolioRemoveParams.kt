package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.squareup.moshi.JsonClass

@Stable
data class PortfolioRemoveParams(
    val symbol: StockSymbol,
    val holdingId: DbHolding.Id,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        symbol = symbol.raw,
        holdingId = holdingId.raw,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json
  internal constructor(
      val symbol: String,
      val holdingId: String,
  ) {

    @CheckResult
    fun fromJson(): PortfolioRemoveParams {
      return PortfolioRemoveParams(
          symbol = symbol.asSymbol(),
          holdingId = DbHolding.Id(holdingId),
      )
    }
  }
}
