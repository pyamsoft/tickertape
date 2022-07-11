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

import android.os.Bundle
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

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    val s = savedInstanceState.get<String>(KEY_SCREEN_ID)
    if (s != null) {
      val restored =
          when (s) {
            MainPage.Home::class.java.name -> MainPage.Home
            MainPage.WatchList::class.java.name -> MainPage.WatchList
            MainPage.WatchListDig::class.java.name -> MainPage.WatchListDig
            MainPage.Portfolio::class.java.name -> MainPage.Portfolio
            else -> throw IllegalArgumentException("Unable to restore screen: $s")
          }
      updateCurrentScreen(restored)
    }
  }

  override fun saveState(outState: UiSavedStateWriter) {
    val s = currentScreen()
    if (s != null) {
      outState.put(KEY_SCREEN_ID, s::class.java.name)
    } else {
      outState.remove<String>(KEY_SCREEN_ID)
    }
  }

  override fun performFragmentTransaction(
      container: Int,
      data: FragmentTag,
      newScreen: Navigator.Screen<MainPage>,
      previousScreen: MainPage?
  ) {
    if (newScreen.screen == MainPage.WatchListDig) {
      commit {
        decideAnimationForPage(newScreen.screen, previousScreen)
        add(container, data.fragment(newScreen.arguments), data.tag)
        addToBackStack(data.tag)
      }
    } else {
      commitNow {
        decideAnimationForPage(newScreen.screen, previousScreen)
        replace(container, data.fragment(newScreen.arguments), data.tag)

        apply {
          if (newScreen.screen == MainPage.WatchListDig) {
            addToBackStack(data.tag)
          }
        }
      }
    }
  }

  override fun provideFragmentTagMap(): Map<MainPage, FragmentTag> {
    return mapOf(
        MainPage.Home to createFragmentTag("HomeFragment") { HomeFragment.newInstance() },
        MainPage.Portfolio to
            createFragmentTag("PortfolioFragment") { PortfolioFragment.newInstance() },
        MainPage.WatchList to
            createFragmentTag("WatchListFragment") { WatchlistFragment.newInstance() },
        MainPage.WatchListDig to
            createFragmentTag("WatchListDigFragment") { WatchlistDigFragment.newInstance(it) },
    )
  }

  companion object {

    private const val KEY_SCREEN_ID = "key_screen_id"

    private fun FragmentTransaction.decideAnimationForPage(newPage: MainPage, oldPage: MainPage?) {
      val animations =
          when (newPage) {
            is MainPage.WatchList ->
                when (oldPage) {
                  null -> R.anim.fragment_open_enter to R.anim.fragment_open_exit
                  is MainPage.WatchListDig ->
                      R.anim.fragment_open_enter to R.anim.fragment_open_exit
                  is MainPage.Home -> R.anim.slide_in_right to R.anim.slide_out_left
                  is MainPage.Portfolio -> R.anim.slide_in_left to R.anim.slide_out_right
                  is MainPage.WatchList -> null
                }
            is MainPage.Home ->
                when (oldPage) {
                  is MainPage.WatchListDig ->
                      R.anim.fragment_open_enter to R.anim.fragment_open_exit
                  null -> R.anim.fragment_open_enter to R.anim.fragment_open_exit
                  is MainPage.WatchList, is MainPage.Portfolio ->
                      R.anim.slide_in_left to R.anim.slide_out_right
                  is MainPage.Home -> null
                }
            is MainPage.Portfolio ->
                when (oldPage) {
                  is MainPage.WatchListDig ->
                      R.anim.fragment_open_enter to R.anim.fragment_open_exit
                  null -> R.anim.fragment_open_enter to R.anim.fragment_open_exit
                  is MainPage.WatchList, is MainPage.Home ->
                      R.anim.slide_in_right to R.anim.slide_out_left
                  is MainPage.Portfolio -> null
                }
            is MainPage.WatchListDig -> null
          }

      if (animations != null) {
        val (enter, exit) = animations
        setCustomAnimations(enter, exit, enter, exit)
      }
    }

    @JvmStatic
    @CheckResult
    private inline fun createFragmentTag(
        tag: String,
        crossinline fragment: (arguments: Bundle?) -> Fragment,
    ): FragmentTag {
      return object : FragmentTag {
        override val fragment: (arguments: Bundle?) -> Fragment = { fragment(it) }
        override val tag: String = tag
      }
    }
  }
}
