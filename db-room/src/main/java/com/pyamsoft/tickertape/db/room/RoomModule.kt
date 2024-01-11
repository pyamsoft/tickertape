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

import android.content.Context
import androidx.annotation.CheckResult
import androidx.room.Room
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.TickerDb
import com.pyamsoft.tickertape.db.holding.HoldingDeleteDao
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverDeleteDao
import com.pyamsoft.tickertape.db.mover.BigMoverInsertDao
import com.pyamsoft.tickertape.db.mover.BigMoverQueryDao
import com.pyamsoft.tickertape.db.position.PositionDeleteDao
import com.pyamsoft.tickertape.db.position.PositionInsertDao
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertDeleteDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertInsertDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertQueryDao
import com.pyamsoft.tickertape.db.split.SplitDeleteDao
import com.pyamsoft.tickertape.db.split.SplitInsertDao
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) private annotation class InternalApi

@Module
abstract class RoomModule {

  @Binds @CheckResult internal abstract fun bindDb(impl: TickerDbImpl): TickerDb

  @Module
  companion object {

    private const val DB_NAME = "tickertape_room_db.db"

    @Provides
    @JvmStatic
    @CheckResult
    @InternalApi
    internal fun provideRoom(context: Context): RoomTickerDb {
      val appContext = context.applicationContext
      return Room.databaseBuilder(appContext, RoomTickerDbImpl::class.java, DB_NAME).build()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomHoldingQueryDao(@InternalApi db: RoomTickerDb): HoldingQueryDao {
      return db.roomHoldingQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomHoldingInsertDao(@InternalApi db: RoomTickerDb): HoldingInsertDao {
      return db.roomHoldingInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomHoldingDeleteDao(@InternalApi db: RoomTickerDb): HoldingDeleteDao {
      return db.roomHoldingDeleteDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomPositionQueryDao(@InternalApi db: RoomTickerDb): PositionQueryDao {
      return db.roomPositionQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomPositionInsertDao(@InternalApi db: RoomTickerDb): PositionInsertDao {
      return db.roomPositionInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomPositionDeleteDao(@InternalApi db: RoomTickerDb): PositionDeleteDao {
      return db.roomPositionDeleteDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomBigMoverQueryDao(@InternalApi db: RoomTickerDb): BigMoverQueryDao {
      return db.roomBigMoverQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomBigMoverInsertDao(@InternalApi db: RoomTickerDb): BigMoverInsertDao {
      return db.roomBigMoverInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomBigMoverDeleteDao(@InternalApi db: RoomTickerDb): BigMoverDeleteDao {
      return db.roomBigMoverDeleteDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomSplitQueryDao(@InternalApi db: RoomTickerDb): SplitQueryDao {
      return db.roomSplitQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomSplitInsertDao(@InternalApi db: RoomTickerDb): SplitInsertDao {
      return db.roomSplitInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomSplitDeleteDao(@InternalApi db: RoomTickerDb): SplitDeleteDao {
      return db.roomSplitDeleteDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomPriceAlertQueryDao(@InternalApi db: RoomTickerDb): PriceAlertQueryDao {
      return db.roomPriceAlertQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomPriceAlertInsertDao(
        @InternalApi db: RoomTickerDb
    ): PriceAlertInsertDao {
      return db.roomPriceAlertInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomPriceAlertDeleteDao(
        @InternalApi db: RoomTickerDb
    ): PriceAlertDeleteDao {
      return db.roomPriceAlertDeleteDao()
    }
  }
}
