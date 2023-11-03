/*
 * Copyright 2023 pyamsoft
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

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.room.holding.dao.RoomHoldingDeleteDao
import com.pyamsoft.tickertape.db.room.holding.dao.RoomHoldingInsertDao
import com.pyamsoft.tickertape.db.room.holding.dao.RoomHoldingQueryDao
import com.pyamsoft.tickertape.db.room.mover.dao.RoomBigMoverDeleteDao
import com.pyamsoft.tickertape.db.room.mover.dao.RoomBigMoverInsertDao
import com.pyamsoft.tickertape.db.room.mover.dao.RoomBigMoverQueryDao
import com.pyamsoft.tickertape.db.room.position.dao.RoomPositionDeleteDao
import com.pyamsoft.tickertape.db.room.position.dao.RoomPositionInsertDao
import com.pyamsoft.tickertape.db.room.position.dao.RoomPositionQueryDao
import com.pyamsoft.tickertape.db.room.pricealert.dao.RoomPriceAlertDeleteDao
import com.pyamsoft.tickertape.db.room.pricealert.dao.RoomPriceAlertInsertDao
import com.pyamsoft.tickertape.db.room.pricealert.dao.RoomPriceAlertQueryDao
import com.pyamsoft.tickertape.db.room.split.dao.RoomSplitDeleteDao
import com.pyamsoft.tickertape.db.room.split.dao.RoomSplitInsertDao
import com.pyamsoft.tickertape.db.room.split.dao.RoomSplitQueryDao

internal interface RoomTickerDb {

  // Holdings
  @CheckResult fun roomHoldingQueryDao(): RoomHoldingQueryDao

  @CheckResult fun roomHoldingInsertDao(): RoomHoldingInsertDao

  @CheckResult fun roomHoldingDeleteDao(): RoomHoldingDeleteDao

  // Positions
  @CheckResult fun roomPositionQueryDao(): RoomPositionQueryDao

  @CheckResult fun roomPositionInsertDao(): RoomPositionInsertDao

  @CheckResult fun roomPositionDeleteDao(): RoomPositionDeleteDao

  // Big Movers
  @CheckResult fun roomBigMoverQueryDao(): RoomBigMoverQueryDao

  @CheckResult fun roomBigMoverInsertDao(): RoomBigMoverInsertDao

  @CheckResult fun roomBigMoverDeleteDao(): RoomBigMoverDeleteDao

  // Splits
  @CheckResult fun roomSplitQueryDao(): RoomSplitQueryDao

  @CheckResult fun roomSplitInsertDao(): RoomSplitInsertDao

  @CheckResult fun roomSplitDeleteDao(): RoomSplitDeleteDao

  // Price Alerts
  @CheckResult fun roomPriceAlertQueryDao(): RoomPriceAlertQueryDao

  @CheckResult fun roomPriceAlertInsertDao(): RoomPriceAlertInsertDao

  @CheckResult fun roomPriceAlertDeleteDao(): RoomPriceAlertDeleteDao
}
