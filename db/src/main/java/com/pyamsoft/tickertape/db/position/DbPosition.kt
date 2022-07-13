/*
 * Copyright 2021 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.db.position

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDateTime

interface DbPosition {

  @CheckResult fun id(): Id

  @CheckResult fun holdingId(): DbHolding.Id

  @CheckResult fun price(): StockMoneyValue

  @CheckResult fun price(price: StockMoneyValue): DbPosition

  @CheckResult fun shareCount(): StockShareValue

  @CheckResult fun shareCount(shareCount: StockShareValue): DbPosition

  @CheckResult fun purchaseDate(): LocalDateTime

  @CheckResult fun purchaseDate(purchaseDate: LocalDateTime): DbPosition

  data class Id(val id: String) {

    @CheckResult
    fun isEmpty(): Boolean {
      return id.isBlank()
    }

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }
}

@CheckResult
private fun DbPosition.getAffectingSplits(splits: List<DbSplit>): List<DbSplit> {
  // No splits, no work
  if (splits.isEmpty()) {
    return emptyList()
  }

  return splits.filter { s ->
    val pd = this.purchaseDate()
    val date = s.splitDate()
    return@filter date.isAfter(pd) || date.isEqual(pd)
  }
}

@CheckResult
fun DbPosition.priceWithSplits(splits: List<DbSplit>): StockMoneyValue {
  val affectingSplits = this.getAffectingSplits(splits)

  // No affecting splits, no further work
  if (affectingSplits.isEmpty()) {
    return this.price()
  }

  // For price calculations, see https://github.com/pyamsoft/tickertape/issues/84
  var raw = this.price().value()
  for (split in affectingSplits) {
    val pre = split.preSplitShareCount()
    val post = split.postSplitShareCount()
    raw = (raw / post.value()) * pre.value()
  }

  return raw.asMoney()
}

@CheckResult
fun DbPosition.shareCountWithSplits(splits: List<DbSplit>): StockShareValue {
  val affectingSplits = this.getAffectingSplits(splits)

  // No affecting splits, no further work
  if (affectingSplits.isEmpty()) {
    return this.shareCount()
  }

  // For price calculations, see https://github.com/pyamsoft/tickertape/issues/84
  var raw = this.shareCount().value()
  for (split in affectingSplits) {
    val pre = split.preSplitShareCount()
    val post = split.postSplitShareCount()
    raw = (raw / pre.value()) * post.value()
  }

  return raw.asShares()
}
