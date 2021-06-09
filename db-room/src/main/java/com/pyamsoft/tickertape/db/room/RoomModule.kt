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

import android.content.Context
import androidx.annotation.CheckResult
import androidx.room.Room
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.DbCache
import com.pyamsoft.tickertape.db.TickerDb
import com.pyamsoft.tickertape.db.symbol.SymbolDeleteDao
import com.pyamsoft.tickertape.db.symbol.SymbolInsertDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class InternalApi

@Module
abstract class RoomModule {

    @Binds
    @CheckResult
    internal abstract fun provideDb(impl: TickerDbImpl): TickerDb

    @Binds
    @CheckResult
    internal abstract fun provideDbCache(impl: TickerDbImpl): DbCache

    @Module
    companion object {

        private const val DB_NAME = "tickertape_room_db.db"

        @Provides
        @JvmStatic
        @CheckResult
        @InternalApi
        internal fun provideRoom(context: Context): RoomTickerDb {
            val appContext = context.applicationContext
            return Room.databaseBuilder(appContext, RoomTickerDbImpl::class.java, DB_NAME)
                .build()
        }

        @DbApi
        @Provides
        @JvmStatic
        internal fun provideRoomSymbolQueryDao(@InternalApi db: RoomTickerDb): SymbolQueryDao {
            return db.roomSymbolQueryDao()
        }

        @DbApi
        @Provides
        @JvmStatic
        internal fun provideRoomSymbolInsertDao(@InternalApi db: RoomTickerDb): SymbolInsertDao {
            return db.roomSymbolInsertDao()
        }

        @DbApi
        @Provides
        @JvmStatic
        internal fun provideRoomSymbolDeleteDao(@InternalApi db: RoomTickerDb): SymbolDeleteDao {
            return db.roomSymbolDeleteDao()
        }

    }
}
