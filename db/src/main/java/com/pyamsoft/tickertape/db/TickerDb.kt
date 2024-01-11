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

package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.holding.HoldingDb
import com.pyamsoft.tickertape.db.mover.BigMoverDb
import com.pyamsoft.tickertape.db.position.PositionDb
import com.pyamsoft.tickertape.db.pricealert.PriceAlertDb
import com.pyamsoft.tickertape.db.split.SplitDb

interface TickerDb {

  @get:CheckResult val holdings: HoldingDb

  @get:CheckResult val positions: PositionDb

  @get:CheckResult val bigMovers: BigMoverDb

  @get:CheckResult val splits: SplitDb

  @get:CheckResult val priceAlerts: PriceAlertDb

  suspend fun invalidate()
}
