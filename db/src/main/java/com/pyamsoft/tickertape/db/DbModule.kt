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
import com.pyamsoft.tickertape.db.holding.HoldingDb
import com.pyamsoft.tickertape.db.holding.HoldingDbImpl
import com.pyamsoft.tickertape.db.holding.HoldingDeleteDao
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.holding.HoldingRealtime
import com.pyamsoft.tickertape.db.mover.BigMoverDb
import com.pyamsoft.tickertape.db.mover.BigMoverDbImpl
import com.pyamsoft.tickertape.db.mover.BigMoverDeleteDao
import com.pyamsoft.tickertape.db.mover.BigMoverInsertDao
import com.pyamsoft.tickertape.db.mover.BigMoverQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverRealtime
import com.pyamsoft.tickertape.db.position.PositionDb
import com.pyamsoft.tickertape.db.position.PositionDbImpl
import com.pyamsoft.tickertape.db.position.PositionDeleteDao
import com.pyamsoft.tickertape.db.position.PositionInsertDao
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.position.PositionRealtime
import com.pyamsoft.tickertape.db.pricealert.PriceAlertDb
import com.pyamsoft.tickertape.db.pricealert.PriceAlertDbImpl
import com.pyamsoft.tickertape.db.pricealert.PriceAlertDeleteDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertInsertDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertQueryDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertRealtime
import com.pyamsoft.tickertape.db.split.SplitDb
import com.pyamsoft.tickertape.db.split.SplitDbImpl
import com.pyamsoft.tickertape.db.split.SplitDeleteDao
import com.pyamsoft.tickertape.db.split.SplitInsertDao
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import com.pyamsoft.tickertape.db.split.SplitRealtime
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

@Qualifier @Retention(AnnotationRetention.BINARY) private annotation class InternalApi

@Module
abstract class DbModule {

  // DB

  @Binds @CheckResult internal abstract fun provideSymbolDbImpl(impl: SymbolDbImpl): SymbolDb

  @Binds @CheckResult internal abstract fun provideHoldingDbImpl(impl: HoldingDbImpl): HoldingDb

  @Binds @CheckResult internal abstract fun providePositionDbImpl(impl: PositionDbImpl): PositionDb

  @Binds @CheckResult internal abstract fun provideBigMoverDbImpl(impl: BigMoverDbImpl): BigMoverDb

  @Binds @CheckResult internal abstract fun provideSplitDbImpl(impl: SplitDbImpl): SplitDb

  @Binds
  @CheckResult
  internal abstract fun providePriceAlertDbImpl(impl: PriceAlertDbImpl): PriceAlertDb

  // Caches

  @Binds
  @CheckResult
  internal abstract fun provideSymbolCache(impl: SymbolDbImpl): SymbolQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideHoldingCache(impl: HoldingDbImpl): HoldingQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun providePositionCache(impl: PositionDbImpl): PositionQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideBigMoverCache(impl: BigMoverDbImpl): BigMoverQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideSplitCache(impl: SplitDbImpl): SplitQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun providePriceAlertCache(impl: PriceAlertDbImpl): PriceAlertQueryDao.Cache

  @Module
  companion object {

    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideSymbolDb(db: TickerDb): SymbolDb {
      return db.symbols
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSymbolRealtimeDao(@InternalApi db: SymbolDb): SymbolRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSymbolQueryDao(@InternalApi db: SymbolDb): SymbolQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSymbolInsertDao(@InternalApi db: SymbolDb): SymbolInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSymbolDeleteDao(@InternalApi db: SymbolDb): SymbolDeleteDao {
      return db.deleteDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideHoldingDb(db: TickerDb): HoldingDb {
      return db.holdings
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideHoldingRealtimeDao(@InternalApi db: HoldingDb): HoldingRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideHoldingQueryDao(@InternalApi db: HoldingDb): HoldingQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideHoldingInsertDao(@InternalApi db: HoldingDb): HoldingInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideHoldingDeleteDao(@InternalApi db: HoldingDb): HoldingDeleteDao {
      return db.deleteDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun providePositionDb(db: TickerDb): PositionDb {
      return db.positions
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePositionRealtimeDao(@InternalApi db: PositionDb): PositionRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePositionQueryDao(@InternalApi db: PositionDb): PositionQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePositionInsertDao(@InternalApi db: PositionDb): PositionInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePositionDeleteDao(@InternalApi db: PositionDb): PositionDeleteDao {
      return db.deleteDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideBigMoverDb(db: TickerDb): BigMoverDb {
      return db.bigMovers
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideBigMoverRealtimeDao(@InternalApi db: BigMoverDb): BigMoverRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideBigMoverQueryDao(@InternalApi db: BigMoverDb): BigMoverQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideBigMoverInsertDao(@InternalApi db: BigMoverDb): BigMoverInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideBigMoverDeleteDao(@InternalApi db: BigMoverDb): BigMoverDeleteDao {
      return db.deleteDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideSplitDb(db: TickerDb): SplitDb {
      return db.splits
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSplitRealtimeDao(@InternalApi db: SplitDb): SplitRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSplitQueryDao(@InternalApi db: SplitDb): SplitQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSplitInsertDao(@InternalApi db: SplitDb): SplitInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSplitDeleteDao(@InternalApi db: SplitDb): SplitDeleteDao {
      return db.deleteDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun providePriceAlertDb(db: TickerDb): PriceAlertDb {
      return db.priceAlerts
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePriceAlertRealtimeDao(@InternalApi db: PriceAlertDb): PriceAlertRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePriceAlertQueryDao(@InternalApi db: PriceAlertDb): PriceAlertQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePriceAlertInsertDao(@InternalApi db: PriceAlertDb): PriceAlertInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun providePriceAlertDeleteDao(@InternalApi db: PriceAlertDb): PriceAlertDeleteDao {
      return db.deleteDao
    }
  }
}
