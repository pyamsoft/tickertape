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

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.installPYDroid
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.ChangeLogProvider
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.navigator.Navigator
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.pydroid.util.doOnCreate
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverNotificationData
import com.pyamsoft.tickertape.databinding.ActivityMainBinding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigEntry
import javax.inject.Inject
import timber.log.Timber

internal class MainActivity : AppCompatActivity() {

  private var viewBinding: ActivityMainBinding? = null

  @JvmField @Inject internal var launcher: MainAlarmLauncher? = null
  @JvmField @Inject internal var navigator: Navigator<MainPage>? = null
  @JvmField @Inject internal var viewModel: MainViewModeler? = null

  init {
    doOnCreate {
      installPYDroid(
          provider =
              object : ChangeLogProvider {

                override val applicationIcon = R.mipmap.ic_launcher

                override val changelog: ChangeLogBuilder = buildChangeLog {}
              },
      )
    }
  }

  private fun onMainActionSelected(page: TopLevelMainPage) {
    viewModel
        .requireNotNull()
        .handleMainActionSelected(
            scope = lifecycleScope,
            page = page,
        )
  }

  private inline fun <T : Any> retrieveFromIntent(
      key: String,
      cast: (String) -> T,
  ): T? {
    val launchIntent = intent
    if (launchIntent == null) {
      Timber.w("Missing launch intent")
      return null
    }

    val symbolString = launchIntent.getStringExtra(key)
    if (symbolString == null) {
      Timber.w("Missing launch key: $key")
      return null
    }

    launchIntent.removeExtra(key)
    return cast(symbolString)
  }

  private fun handleLaunchIntent() {
    val symbol = retrieveFromIntent(BigMoverNotificationData.INTENT_KEY_SYMBOL) { it.asSymbol() }
    if (symbol == null) {
      Timber.w("Cannot open Watchlist Dig Dialog without symbol")
      return
    }

    val equityType =
        retrieveFromIntent(BigMoverNotificationData.INTENT_KEY_EQUITY_TYPE) {
          EquityType.valueOf(it)
        }
    if (equityType == null) {
      Timber.w("Cannot open Watchlist Dig Dialog without equityType")
      return
    }

    val lookupSymbol =
        retrieveFromIntent(BigMoverNotificationData.INTENT_KEY_LOOKUP_SYMBOL) { it.asSymbol() }
    if (lookupSymbol == null) {
      Timber.w("Cannot open Watchlist Dig Dialog without lookupSymbol")
      return
    }

    Timber.d("Launch intent with symbol: $symbol")
    launcher.requireNotNull().cancelNotifications(symbol)

    viewModel
        .requireNotNull()
        .handleOpenDig(
            symbol = symbol,
            lookupSymbol = lookupSymbol,
            equityType = equityType,
        )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // NOTE(Peter):
    // Not full Compose yet
    // Compose has an issue handling Fragments.
    //
    // We need an AndroidView to handle a Fragment, but a Fragment outlives the Activity via the
    // FragmentManager keeping state. The Compose render does not, so when an activity dies from
    // configuration change, the Fragment is headless somewhere in the great beyond. This leads to
    // memory leaks and other issues like Disposable hooks not being called on DisposeEffect blocks.
    // To avoid these growing pains, we use an Activity layout file and then host the ComposeViews
    // from it that are then used to render Activity level views. Fragment transactions happen as
    // normal and then Fragments host ComposeViews too.
    val binding = ActivityMainBinding.inflate(layoutInflater).apply { viewBinding = this }
    setContentView(binding.root)

    val component =
        ObjectGraph.ApplicationScope.retrieve(this)
            .plusMainComponent()
            .create(
                this,
                binding.mainFragmentContainerView.id,
            )
    component.inject(this)
    ObjectGraph.ActivityScope.install(this, component)

    setTheme(R.style.Theme_TickerTape)
    super.onCreate(savedInstanceState)
    stableLayoutHideNavigation()

    handleLaunchIntent()

    // Snackbar respects window offsets and hosts snackbar composables
    // Because these are not in a nice Scaffold, we cannot take advantage of Coordinator style
    // actions (a FAB will not move out of the way for example)
    val navi = navigator.requireNotNull()
    val vm = viewModel.requireNotNull()

    vm.restoreState(savedInstanceState)

    binding.mainComposeBottom.setContent {
      val screen by navi.currentScreenState()
      val page = remember(screen) { screen as? TopLevelMainPage }

      val state = vm.state
      val theme by state.theme.collectAsState()
      val watchlistDig by state.watchlistDigParams.collectAsState()

      TickerTapeTheme(theme) {
        SystemBars(theme, screen)

        Crossfade(
            targetState = watchlistDig,
        ) { dig ->
          if (dig == null) {
            // Need to have box or snackbars push up bottom bar
            Box(
                contentAlignment = Alignment.BottomCenter,
            ) {
              page?.also { p ->
                MainScreen(
                    page = p,
                    onLoadPage = { navi.navigateTo(it) },
                    onActionSelected = { onMainActionSelected(it) },
                )
              }
            }
          } else {
            WatchlistDigEntry(
                modifier = Modifier.fillMaxSize(),
                params = dig,
                onGoBack = { vm.handleCloseDig() },
            )
          }
        }
      }
    }

    vm.handleSyncDarkTheme(this)

    navi.restoreState(savedInstanceState)
    navi.loadIfEmpty { TopLevelMainPage.Home }

    launcher
        .requireNotNull()
        .bind(
            scope = lifecycleScope,
            lifecycle = lifecycle,
        )
  }

  override fun onResume() {
    super.onResume()

    // Vitals
    reportFullyDrawn()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleLaunchIntent()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    viewModel?.handleSyncDarkTheme(this)
    viewBinding?.apply { mainComposeBottom.recompose() }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
    navigator?.saveState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    viewBinding?.apply { this.mainComposeBottom.dispose() }

    viewBinding = null
    launcher = null
    navigator = null
    viewModel = null
  }
}
