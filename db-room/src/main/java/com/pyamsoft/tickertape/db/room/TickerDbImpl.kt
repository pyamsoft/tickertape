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
package com.pyamsoft.tickertape.db.room

import com.pyamsoft.tickertape.db.TickerDb
import com.pyamsoft.tickertape.db.holding.HoldingDb
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverDb
import com.pyamsoft.tickertape.db.mover.BigMoverQueryDao
import com.pyamsoft.tickertape.db.position.PositionDb
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertDb
import com.pyamsoft.tickertape.db.pricealert.PriceAlertQueryDao
import com.pyamsoft.tickertape.db.split.SplitDb
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TickerDbImpl
@Inject
internal constructor(
    // DB
    override val holdings: HoldingDb,
    override val positions: PositionDb,
    override val bigMovers: BigMoverDb,
    override val splits: SplitDb,
    override val priceAlerts: PriceAlertDb,

    // Caches
    private val holdingCache: HoldingQueryDao.Cache,
    private val positionCache: PositionQueryDao.Cache,
    private val bigMoverCache: BigMoverQueryDao.Cache,
    private val splitCache: SplitQueryDao.Cache,
    private val priceAlertCache: PriceAlertQueryDao.Cache,
) : TickerDb {

  override suspend fun invalidate() {
    holdingCache.invalidate()
    positionCache.invalidate()
    bigMoverCache.invalidate()
    splitCache.invalidate()
    priceAlertCache.invalidate()
  }
}
