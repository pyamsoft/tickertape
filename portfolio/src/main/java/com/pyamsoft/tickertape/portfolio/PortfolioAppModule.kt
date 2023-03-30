/*
 * Copyright 2023 pyamsoft
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
abstract class PortfolioAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindPortfolioProcessor(
      impl: PortfolioProcessorImpl,
  ): PortfolioProcessor

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
