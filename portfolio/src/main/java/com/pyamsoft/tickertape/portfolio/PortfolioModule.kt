package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractor
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractorImpl
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddInteractor
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddInteractorImpl
import com.pyamsoft.tickertape.portfolio.dig.splits.add.SplitAddInteractor
import com.pyamsoft.tickertape.portfolio.dig.splits.add.SplitAddInteractorImpl
import dagger.Binds
import dagger.Module

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
}
