package com.pyamsoft.tickertape.watchlist

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigInteractor
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class WatchlistModule {

  @Binds
  @CheckResult
  internal abstract fun bindWatchlistInteractor(
      impl: WatchlistInteractorImpl,
  ): WatchlistInteractor

    @Binds
    @CheckResult
    internal abstract fun bindWatchlistInteractorCache(
        impl: WatchlistInteractorImpl,
    ): WatchlistInteractor.Cache

  @Binds
  @CheckResult
  internal abstract fun bindWatchlistDigInteractor(
      impl: WatchlistDigInteractorImpl,
  ): WatchlistDigInteractor

    @Binds
    @CheckResult
    internal abstract fun bindWatchlistDigInteractorCache(
        impl: WatchlistDigInteractorImpl,
    ): WatchlistDigInteractor.Cache
}
