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
import com.pyamsoft.pydroid.ui.navigator.Navigator
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
) : FragmentNavigator(activity, fragmentContainerId) {

  override fun onRestoreState(savedInstanceState: UiSavedStateReader) {}

  override fun onSaveState(outState: UiSavedStateWriter) {}

  override fun performFragmentTransaction(
      container: Int,
      newScreen: Fragment,
      previousScreen: Fragment?
  ) {
    val tag = Navigator.getTagForScreen(newScreen)
    if (isTopLevelScreen(tag)) {
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

    private val HOME_TAG = Navigator.getTagForScreen(HomeFragment)
    private val WATCHLIST_TAG = Navigator.getTagForScreen(WatchlistFragment)
    private val PORTFOLIO_TAG = Navigator.getTagForScreen(PortfolioFragment)

    private val WATCHLIST_DIG_TAG = Navigator.getTagForScreen(WatchlistDigFragment)
    private val PORTFOLIO_DIG_TAG = Navigator.getTagForScreen(PortfolioDigFragment)

    private data class FragmentAnimation(
        @AnimRes val enter: Int,
        @AnimRes val exit: Int,
    )

    @CheckResult
    private fun isTopLevelScreen(tag: String): Boolean {
      return tag == HOME_TAG || tag == WATCHLIST_TAG || tag == PORTFOLIO_TAG
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
      val newTag = Navigator.getTagForScreen(newPage)
      val oldTag = if (oldPage == null) null else Navigator.getTagForScreen(oldPage)

      val animations =
          when (newTag) {
            HOME_TAG ->
                when (oldTag) {
                  WATCHLIST_TAG, PORTFOLIO_TAG -> R.anim.slide_in_left then R.anim.slide_out_right
                  null, WATCHLIST_DIG_TAG, PORTFOLIO_DIG_TAG ->
                      R.anim.fragment_open_enter then R.anim.fragment_open_exit
                  else -> null
                }
            WATCHLIST_TAG ->
                when (oldTag) {
                  null, WATCHLIST_DIG_TAG, PORTFOLIO_DIG_TAG ->
                      R.anim.fragment_open_enter then R.anim.fragment_open_exit
                  HOME_TAG -> R.anim.slide_in_right then R.anim.slide_out_left
                  PORTFOLIO_TAG -> R.anim.slide_in_left then R.anim.slide_out_right
                  else -> null
                }
            PORTFOLIO_TAG ->
                when (oldTag) {
                  null, WATCHLIST_DIG_TAG, PORTFOLIO_DIG_TAG ->
                      R.anim.fragment_open_enter then R.anim.fragment_open_exit
                  WATCHLIST_TAG, HOME_TAG -> R.anim.slide_in_right then R.anim.slide_out_left
                  else -> null
                }
            WATCHLIST_DIG_TAG, PORTFOLIO_DIG_TAG ->
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
