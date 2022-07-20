package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.LocalDateTime

internal fun newTestPosition(): DbPosition {
  return object : DbPosition {
    override val id: DbPosition.Id = DbPosition.Id.EMPTY
    override val holdingId: DbHolding.Id = DbHolding.Id.EMPTY
    override val price: StockMoneyValue = StockMoneyValue.NONE
    override val shareCount: StockShareValue = StockShareValue.NONE
    override val purchaseDate: LocalDateTime = LocalDateTime.now()

    override fun price(price: StockMoneyValue): DbPosition {
      return this
    }

    override fun shareCount(shareCount: StockShareValue): DbPosition {
      return this
    }

    override fun purchaseDate(purchaseDate: LocalDateTime): DbPosition {
      return this
    }
  }
}
