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
package com.pyamsoft.tickertape.db.room

import com.pyamsoft.tickertape.db.TickerDb
import com.pyamsoft.tickertape.db.holding.HoldingDb
import com.pyamsoft.tickertape.db.mover.BigMoverDb
import com.pyamsoft.tickertape.db.position.PositionDb
import com.pyamsoft.tickertape.db.symbol.SymbolDb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TickerDbImpl
@Inject
internal constructor(
    private val symbolDb: SymbolDb,
    private val holdingDb: HoldingDb,
    private val positionDb: PositionDb,
    private val bigMoverDb: BigMoverDb,
) : TickerDb {

  override fun symbols(): SymbolDb {
    return symbolDb
  }

  override fun holdings(): HoldingDb {
    return holdingDb
  }

  override fun positions(): PositionDb {
    return positionDb
  }

  override fun bigMover(): BigMoverDb {
    return bigMoverDb
  }

  override suspend fun invalidate() {
    symbolDb.invalidate()
    holdingDb.invalidate()
    positionDb.invalidate()
    bigMoverDb.invalidate()
  }
}
