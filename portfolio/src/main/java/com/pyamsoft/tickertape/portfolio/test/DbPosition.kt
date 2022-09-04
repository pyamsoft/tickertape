package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDate

internal fun newTestPosition(): DbPosition {
  return object : DbPosition {
    override val id: DbPosition.Id = DbPosition.Id.EMPTY
    override val holdingId: DbHolding.Id = DbHolding.Id.EMPTY
    override val price: StockMoneyValue = 1.0.asMoney()
    override val shareCount: StockShareValue = 5.0.asShares()
    override val purchaseDate: LocalDate = LocalDate.now()

    override fun price(price: StockMoneyValue): DbPosition {
      return this
    }

    override fun shareCount(shareCount: StockShareValue): DbPosition {
      return this
    }

    override fun purchaseDate(purchaseDate: LocalDate): DbPosition {
      return this
    }
  }
}
