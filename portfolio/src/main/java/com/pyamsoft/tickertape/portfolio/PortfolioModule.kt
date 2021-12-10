package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractor
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigInteractorImpl
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
  internal abstract fun bindPortfolioDigInteractor(
      impl: PortfolioDigInteractorImpl,
  ): PortfolioDigInteractor
}
