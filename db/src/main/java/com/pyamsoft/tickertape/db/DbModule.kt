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

package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.symbol.SymbolDb
import com.pyamsoft.tickertape.db.symbol.SymbolDbImpl
import com.pyamsoft.tickertape.db.symbol.SymbolDeleteDao
import com.pyamsoft.tickertape.db.symbol.SymbolInsertDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.db.symbol.SymbolRealtime
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class InternalApi

@Module
abstract class DbModule {

    @Binds
    @CheckResult
    internal abstract fun provideSymbolDbImpl(impl: SymbolDbImpl): SymbolDb

    @Module
    companion object {

        @JvmStatic
        @Provides
        @CheckResult
        @InternalApi
        internal fun provideSymbolDb(db: TickerDb): SymbolDb {
            return db.symbols()
        }

        @JvmStatic
        @Provides
        @CheckResult
        internal fun provideSymbolRealtimeDao(@InternalApi db: SymbolDb): SymbolRealtime {
            return db.realtime()
        }

        @JvmStatic
        @Provides
        @CheckResult
        internal fun provideSymbolQueryDao(@InternalApi db: SymbolDb): SymbolQueryDao {
            return db.queryDao()
        }

        @JvmStatic
        @Provides
        @CheckResult
        internal fun provideSymbolInsertDao(@InternalApi db: SymbolDb): SymbolInsertDao {
            return db.insertDao()
        }

        @JvmStatic
        @Provides
        @CheckResult
        internal fun provideSymbolDeleteDao(@InternalApi db: SymbolDb): SymbolDeleteDao {
            return db.deleteDao()
        }

    }
}
