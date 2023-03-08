package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.squareup.moshi.JsonClass

@Stable
data class PortfolioDigParams(
    val symbol: StockSymbol,
    val lookupSymbol: StockSymbol?,
    val currentPrice: StockMoneyValue? = null,
    val holding: DbHolding? = null,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        symbol = symbol.raw,
        lookupSymbol = lookupSymbol?.raw,
        currentPrice = currentPrice?.value,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json
  internal constructor(
      val symbol: String,
      val lookupSymbol: String?,
      val currentPrice: Double?,
  ) {

    @CheckResult
    fun fromJson(): PortfolioDigParams {
      return PortfolioDigParams(
          symbol = symbol.asSymbol(),
          lookupSymbol = lookupSymbol?.asSymbol(),
          currentPrice = currentPrice?.asMoney(),
      )
    }
  }
}
