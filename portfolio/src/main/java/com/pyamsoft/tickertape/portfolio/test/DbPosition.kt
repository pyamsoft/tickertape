package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.*
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

    override fun shareCount(): StockShareValue {
      return 1.0.asShares()
    }

    override fun purchaseDate(): LocalDateTime {
      return LocalDateTime.now()
    }
  }
}
