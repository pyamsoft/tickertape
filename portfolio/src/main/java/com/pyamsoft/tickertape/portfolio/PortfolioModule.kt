package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractor
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractorImpl
import com.pyamsoft.tickertape.portfolio.dig.position.add.DatePickerEvent
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddInteractor
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddInteractorImpl
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
  internal abstract fun bindPortfolioDigInteractor(
      impl: PortfolioDigInteractorImpl,
  ): PortfolioDigInteractor

  @Binds
  @CheckResult
  internal abstract fun bindPositionAddInteractor(
      impl: PositionAddInteractorImpl,
  ): PositionAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindDatePickerEventConsumer(
      bus: EventBus<DatePickerEvent>
  ): EventConsumer<DatePickerEvent>

  @Module
  companion object {

    @Provides
    @JvmStatic
    @Singleton
    @CheckResult
    internal fun providePositionDatePickerEventBus(): EventBus<DatePickerEvent> {
      return EventBus.create()
    }
  }
}
