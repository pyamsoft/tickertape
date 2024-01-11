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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pyamsoft.tickertape.db.room.converter.LocalDateConverter
import com.pyamsoft.tickertape.db.room.converter.LocalDateTimeConverter
import com.pyamsoft.tickertape.db.room.converter.StockMoneyValueConverter
import com.pyamsoft.tickertape.db.room.converter.StockShareValueConverter
import com.pyamsoft.tickertape.db.room.converter.StockSymbolConverter
import com.pyamsoft.tickertape.db.room.holding.converter.DbHoldingIdConverter
import com.pyamsoft.tickertape.db.room.holding.converter.EquityTypeConverter
import com.pyamsoft.tickertape.db.room.holding.converter.TradeSideConverter
import com.pyamsoft.tickertape.db.room.holding.entity.RoomDbHolding
import com.pyamsoft.tickertape.db.room.mover.converter.BigMoverReportIdConverter
import com.pyamsoft.tickertape.db.room.mover.converter.MarketStateConverter
import com.pyamsoft.tickertape.db.room.mover.converter.StockPercentConverter
import com.pyamsoft.tickertape.db.room.mover.entity.RoomBigMoverReport
import com.pyamsoft.tickertape.db.room.position.converter.DbPositionIdConverter
import com.pyamsoft.tickertape.db.room.position.entity.RoomDbPosition
import com.pyamsoft.tickertape.db.room.pricealert.converter.PriceAlertIdConverter
import com.pyamsoft.tickertape.db.room.pricealert.entity.RoomPriceAlert
import com.pyamsoft.tickertape.db.room.split.converter.DbSplitIdConverter
import com.pyamsoft.tickertape.db.room.split.entity.RoomDbSplit

@Database(
    exportSchema = true,
    version = 1,
    entities =
        [
            // Version 1
            RoomDbHolding::class,
            RoomDbPosition::class,
            RoomBigMoverReport::class,
            RoomDbSplit::class,
            RoomPriceAlert::class,
        ],
)
@TypeConverters(
    // Version 1
    DbHoldingIdConverter::class,
    DbSplitIdConverter::class,
    BigMoverReportIdConverter::class,
    StockSymbolConverter::class,
    StockShareValueConverter::class,
    StockMoneyValueConverter::class,
    LocalDateTimeConverter::class,
    EquityTypeConverter::class,
    MarketStateConverter::class,
    StockPercentConverter::class,
    TradeSideConverter::class,
    LocalDateConverter::class,
    DbPositionIdConverter::class,
    PriceAlertIdConverter::class,
)
internal abstract class RoomTickerDbImpl internal constructor() : RoomDatabase(), RoomTickerDb
