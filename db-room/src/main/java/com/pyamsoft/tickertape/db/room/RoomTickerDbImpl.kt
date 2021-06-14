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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pyamsoft.tickertape.db.room.converter.DbHoldingIdConverter
import com.pyamsoft.tickertape.db.room.converter.DbPositionIdConverter
import com.pyamsoft.tickertape.db.room.converter.DbSymbolIdConverter
import com.pyamsoft.tickertape.db.room.converter.StockMoneyValueConverter
import com.pyamsoft.tickertape.db.room.converter.StockSymbolConverter
import com.pyamsoft.tickertape.db.room.entity.RoomDbHolding
import com.pyamsoft.tickertape.db.room.entity.RoomDbPosition
import com.pyamsoft.tickertape.db.room.entity.RoomDbSymbol

@Database(
    version = 2, entities = [RoomDbSymbol::class, RoomDbHolding::class, RoomDbPosition::class])
@TypeConverters(
    DbSymbolIdConverter::class,
    DbHoldingIdConverter::class,
    DbPositionIdConverter::class,
    StockSymbolConverter::class,
    StockMoneyValueConverter::class)
internal abstract class RoomTickerDbImpl internal constructor() : RoomDatabase(), RoomTickerDb
