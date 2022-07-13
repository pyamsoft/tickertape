package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDateTime

internal fun newTestPosition(): DbPosition {
  return object : DbPosition {
    override fun id(): DbPosition.Id {
      return DbPosition.Id("TEST")
    }

    override fun holdingId(): DbHolding.Id {
      return DbHolding.Id("TEST")
    }

    override fun price(): StockMoneyValue {
      return 1.0.asMoney()
    }

    override fun price(price: StockMoneyValue): DbPosition {
      return this
    }

    override fun shareCount(): StockShareValue {
      return 1.0.asShares()
    }

    override fun shareCount(shareCount: StockShareValue): DbPosition {
      return this
    }

    override fun purchaseDate(): LocalDateTime {
      return LocalDateTime.now()
    }

    override fun purchaseDate(purchaseDate: LocalDateTime): DbPosition {
      return this
    }
  }
}
