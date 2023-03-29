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
  @get:CheckResult val roomHoldingQueryDao: RoomHoldingQueryDao
  @get:CheckResult val roomHoldingInsertDao: RoomHoldingInsertDao
  @get:CheckResult val roomHoldingDeleteDao: RoomHoldingDeleteDao

  // Positions
  @get:CheckResult val roomPositionQueryDao: RoomPositionQueryDao
  @get:CheckResult val roomPositionInsertDao: RoomPositionInsertDao
  @get:CheckResult val roomPositionDeleteDao: RoomPositionDeleteDao

  // Big Movers
  @get:CheckResult val roomBigMoverQueryDao: RoomBigMoverQueryDao
  @get:CheckResult val roomBigMoverInsertDao: RoomBigMoverInsertDao
  @get:CheckResult val roomBigMoverDeleteDao: RoomBigMoverDeleteDao

  // Splits
  @get:CheckResult val roomSplitQueryDao: RoomSplitQueryDao
  @get:CheckResult val roomSplitInsertDao: RoomSplitInsertDao
  @get:CheckResult val roomSplitDeleteDao: RoomSplitDeleteDao

  // Price Alerts
  @get:CheckResult val roomPriceAlertQueryDao: RoomPriceAlertQueryDao
  @get:CheckResult val roomPriceAlertInsertDao: RoomPriceAlertInsertDao
  @get:CheckResult val roomPriceAlertDeleteDao: RoomPriceAlertDeleteDao
}
