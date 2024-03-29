/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.core.isLongTermPurchase
import com.pyamsoft.tickertape.core.isShortTermPurchase
import com.pyamsoft.tickertape.db.IdType
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDate

@Stable
interface DbPosition {

  @get:CheckResult val id: Id

  @get:CheckResult val holdingId: DbHolding.Id

  @get:CheckResult val price: StockMoneyValue

  @get:CheckResult val shareCount: StockShareValue

  @get:CheckResult val purchaseDate: LocalDate

  @CheckResult fun price(price: StockMoneyValue): DbPosition

  @CheckResult fun shareCount(shareCount: StockShareValue): DbPosition

  @CheckResult fun purchaseDate(purchaseDate: LocalDate): DbPosition

  @Stable
  data class Id(
      override val raw: String,
  ) : IdType {

    override val isEmpty: Boolean = raw.isBlank()

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
    val pd = this.purchaseDate
    val date = s.splitDate
    return@filter date.isAfter(pd) || date.isEqual(pd)
  }
}

@CheckResult
fun DbPosition.priceWithSplits(splits: List<DbSplit>): StockMoneyValue {
  val affectingSplits = this.getAffectingSplits(splits)

  // No affecting splits, no further work
  if (affectingSplits.isEmpty()) {
    return this.price
  }

  // For price calculations, see https://github.com/pyamsoft/tickertape/issues/84
  var raw = this.price.value
  for (split in affectingSplits) {
    val pre = split.preSplitShareCount
    val post = split.postSplitShareCount
    raw = (raw / post.value) * pre.value
  }

  return raw.asMoney()
}

@CheckResult
fun DbPosition.shareCountWithSplits(splits: List<DbSplit>): StockShareValue {
  val affectingSplits = this.getAffectingSplits(splits)

  // No affecting splits, no further work
  if (affectingSplits.isEmpty()) {
    return this.shareCount
  }

  // For price calculations, see https://github.com/pyamsoft/tickertape/issues/84
  var raw = this.shareCount.value
  for (split in affectingSplits) {
    val pre = split.preSplitShareCount
    val post = split.postSplitShareCount
    raw = (raw / pre.value) * post.value
  }

  return raw.asShares()
}

@CheckResult
fun DbPosition.isShortTerm(time: LocalDate): Boolean {
  return this.purchaseDate.isShortTermPurchase(time)
}

@CheckResult
fun DbPosition.isLongTerm(time: LocalDate): Boolean {
  return this.purchaseDate.isLongTermPurchase(time)
}
