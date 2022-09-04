package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.LocalDate

internal fun newTestSplit(): DbSplit {
  return object : DbSplit {
    override val id: DbSplit.Id = DbSplit.Id.EMPTY
    override val holdingId: DbHolding.Id = DbHolding.Id.EMPTY
    override val preSplitShareCount: StockShareValue = StockShareValue.NONE
    override val postSplitShareCount: StockShareValue = StockShareValue.NONE
    override val splitDate: LocalDate = LocalDate.now()

    override fun preSplitShareCount(shareCount: StockShareValue): DbSplit {
      return this
    }

    override fun postSplitShareCount(shareCount: StockShareValue): DbSplit {
      return this
    }

    override fun splitDate(date: LocalDate): DbSplit {
      return this
    }
  }
}
