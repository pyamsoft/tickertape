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

package com.pyamsoft.tickertape.main

import androidx.annotation.AnimRes
import androidx.annotation.CheckResult
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.home.HomeFragment
import com.pyamsoft.tickertape.portfolio.PortfolioFragment
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigFragment
import com.pyamsoft.tickertape.watchlist.WatchlistFragment
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigFragment
import javax.inject.Inject

@ActivityScope
internal class MainNavigator
@Inject
internal constructor(
    activity: MainActivity,
    @IdRes fragmentContainerId: Int,
) : FragmentNavigator<MainPage>(activity, fragmentContainerId) {

  override fun onRestoreState(savedInstanceState: UiSavedStateReader) {}

  override fun onSaveState(outState: UiSavedStateWriter) {}

  override fun produceFragmentForScreen(screen: MainPage): Fragment =
      when (screen) {
        is TopLevelMainPage.Home -> HomeFragment.newInstance()
        is TopLevelMainPage.Watchlist -> WatchlistFragment.newInstance()
        is TopLevelMainPage.Portfolio -> PortfolioFragment.newInstance()
        is WatchlistDigFragment.Screen ->
            WatchlistDigFragment.newInstance(
                symbol = screen.symbol,
                lookupSymbol = screen.lookupSymbol,
                allowModifyWatchlist = screen.allowModifyWatchlist,
                equityType = screen.equityType,
            )
        is PortfolioDigFragment.Screen ->
            PortfolioDigFragment.newInstance(
                symbol = screen.symbol,
                lookupSymbol = screen.lookupSymbol,
                holdingId = screen.holdingId,
                holdingType = screen.holdingType,
                holdingSide = screen.holdingSide,
                currentPrice = screen.currentPrice,
            )
        else -> throw IllegalArgumentException("Unhandled screen type: $screen")
      }

  override fun performFragmentTransaction(
      container: Int,
      newScreen: Fragment,
      previousScreen: Fragment?
  ) {
    val tag = newScreen::class.java.name
    if (isTopLevelScreen(newScreen)) {
      commitNow {
        decideAnimationForPage(newScreen, previousScreen)
        replace(container, newScreen, tag)
      }
    } else {
      commit {
        decideAnimationForPage(newScreen, previousScreen)
        add(container, newScreen, tag)
        addToBackStack(tag)
      }
    }
  }

  companion object {

    private data class FragmentAnimation(
        @AnimRes val enter: Int,
        @AnimRes val exit: Int,
    )

    @JvmStatic
    @CheckResult
    private fun isTopLevelScreen(fragment: Fragment): Boolean {
      return fragment is Screen<*> && fragment.getScreenId() is TopLevelMainPage
    }

    @CheckResult
    private infix fun Int.then(exit: Int): FragmentAnimation {
      return FragmentAnimation(
          enter = this,
          exit = exit,
      )
    }

    private fun FragmentTransaction.decideAnimationForPage(
        newPage: Fragment,
        oldPage: Fragment?,
    ) {
      val animations =
          when (newPage) {
            is HomeFragment ->
                when (oldPage) {
                  null, is WatchlistDigFragment, is PortfolioDigFragment ->
                      R.anim.fragment_open_enter then R.anim.fragment_open_exit
                  is WatchlistFragment, is PortfolioFragment ->
                      R.anim.slide_in_left then R.anim.slide_out_right
                  else -> null
                }
            is WatchlistFragment ->
                when (oldPage) {
                  null, is WatchlistDigFragment, is PortfolioDigFragment ->
                      R.anim.fragment_open_enter then R.anim.fragment_open_exit
                  is HomeFragment -> R.anim.slide_in_right then R.anim.slide_out_left
                  is PortfolioFragment -> R.anim.slide_in_left then R.anim.slide_out_right
                  else -> null
                }
            is PortfolioFragment ->
                when (oldPage) {
                  null, is WatchlistDigFragment, is PortfolioDigFragment ->
                      R.anim.fragment_open_enter then R.anim.fragment_open_exit
                  is WatchlistFragment, is HomeFragment ->
                      R.anim.slide_in_right then R.anim.slide_out_left
                  else -> null
                }
            is WatchlistDigFragment, is PortfolioDigFragment ->
                R.anim.fragment_open_enter then R.anim.fragment_open_exit
            else -> null
          }

      if (animations != null) {
        val enter = animations.enter
        val exit = animations.exit
        setCustomAnimations(enter, exit, enter, exit)
      }
    }
  }
}
