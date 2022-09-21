package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractor
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractorImpl
import com.pyamsoft.tickertape.portfolio.dig.base.DateSelectedEvent
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddInteractor
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddInteractorImpl
import com.pyamsoft.tickertape.portfolio.dig.splits.add.SplitAddInteractor
import com.pyamsoft.tickertape.portfolio.dig.splits.add.SplitAddInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class PortfolioModule {

  @Binds
  @CheckResult
  internal abstract fun bindPortfolioInteractor(
      impl: PortfolioInteractorImpl,
  ): PortfolioInteractor

  @Binds
  @CheckResult
  internal abstract fun bindPortfolioInteractorCache(
      impl: PortfolioInteractorImpl,
  ): PortfolioInteractor.Cache

  @Binds
  @CheckResult
  internal abstract fun bindPortfolioDigInteractor(
      impl: PortfolioDigInteractorImpl,
  ): PortfolioDigInteractor

  @Binds
  @CheckResult
  internal abstract fun bindPortfolioDigInteractorCache(
      impl: PortfolioDigInteractorImpl,
  ): PortfolioDigInteractor.Cache

  @Binds
  @CheckResult
  internal abstract fun bindPositionAddInteractor(
      impl: PositionAddInteractorImpl,
  ): PositionAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindSplitAddInteractor(
      impl: SplitAddInteractorImpl,
  ): SplitAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindPositionDatePickerEventConsumer(
      bus: EventBus<DateSelectedEvent<DbPosition.Id>>
  ): EventConsumer<DateSelectedEvent<DbPosition.Id>>

  @Binds
  @CheckResult
  internal abstract fun bindSplitDatePickerEventConsumer(
      bus: EventBus<DateSelectedEvent<DbSplit.Id>>
  ): EventConsumer<DateSelectedEvent<DbSplit.Id>>

  @Module
  companion object {

    @Provides
    @JvmStatic
    @Singleton
    @CheckResult
    internal fun providePositionDatePickerEventBus(): EventBus<DateSelectedEvent<DbPosition.Id>> {
      return EventBus.create()
    }

    @Provides
    @JvmStatic
    @Singleton
    @CheckResult
    internal fun provideSplitDatePickerEventBus(): EventBus<DateSelectedEvent<DbSplit.Id>> {
      return EventBus.create()
    }
  }
}
