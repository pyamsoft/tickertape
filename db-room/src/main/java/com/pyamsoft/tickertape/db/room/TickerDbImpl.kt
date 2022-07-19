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
import com.pyamsoft.tickertape.db.split.SplitDb
import com.pyamsoft.tickertape.db.symbol.SymbolDb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TickerDbImpl
@Inject
internal constructor(
    override val symbols: SymbolDb,
    override val holdings: HoldingDb,
    override val positions: PositionDb,
    override val bigMovers: BigMoverDb,
    override val splits: SplitDb,
) : TickerDb {

  override suspend fun invalidate() {
    symbols.invalidate()
    holdings.invalidate()
    positions.invalidate()
    bigMovers.invalidate()
    splits.invalidate()
  }
}
