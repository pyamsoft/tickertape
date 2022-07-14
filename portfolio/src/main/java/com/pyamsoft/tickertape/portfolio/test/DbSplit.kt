package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDateTime

internal fun newTestSplit(): DbSplit {
  return object : DbSplit {
    override fun id(): DbSplit.Id {
      return DbSplit.Id("TEST")
    }

    override fun holdingId(): DbHolding.Id {
      return DbHolding.Id("TEST")
    }

    override fun preSplitShareCount(): StockShareValue {
      return 1.0.asShares()
    }

    override fun preSplitShareCount(shareCount: StockShareValue): DbSplit {
      return this
    }

    override fun postSplitShareCount(): StockShareValue {
      return 1.0.asShares()
    }

    override fun postSplitShareCount(shareCount: StockShareValue): DbSplit {
      return this
    }

    override fun splitDate(): LocalDateTime {
      return LocalDateTime.now()
    }

    override fun splitDate(date: LocalDateTime): DbSplit {
      return this
    }
  }
}
