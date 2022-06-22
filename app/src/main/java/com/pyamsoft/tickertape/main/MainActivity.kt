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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.insets.ProvideWindowInsets
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.PYDroidActivity
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.navigator.Navigator
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.notification.BigMoverNotificationData
import com.pyamsoft.tickertape.alert.notification.NotificationCanceller
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.databinding.ActivityMainBinding
import com.pyamsoft.tickertape.initOnAppStart
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigDialog
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

internal class MainActivity : PYDroidActivity() {

  override val applicationIcon = R.mipmap.ic_launcher

  override val changelog: ChangeLogBuilder = buildChangeLog {}

  private var viewBinding: ActivityMainBinding? = null
  private var injector: MainComponent? = null

  @JvmField @Inject internal var navigator: Navigator<MainPage>? = null
  @JvmField @Inject internal var notificationCanceller: NotificationCanceller? = null
  @JvmField @Inject internal var tapeLauncher: TapeLauncher? = null
  @JvmField @Inject internal var alerter: Alerter? = null
  @JvmField @Inject internal var alarmFactory: AlarmFactory? = null
  @JvmField @Inject internal var viewModel: MainViewModeler? = null

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

    injector =
        Injector.obtainFromApplication<TickerComponent>(this)
            .plusMainComponent()
            .create(
                this,
                binding.mainFragmentContainerView.id,
            )
            .also { c -> c.inject(this) }
    setTheme(R.style.Theme_TickerTape)
    super.onCreate(savedInstanceState)
    stableLayoutHideNavigation()

    beginWork()
    handleLaunchIntent()

    // Snackbar respects window offsets and hosts snackbar composables
    // Because these are not in a nice Scaffold, we cannot take advantage of Coordinator style
    // actions (a FAB will not move out of the way for example)
    val navi = navigator.requireNotNull()
    val vm = viewModel.requireNotNull()

    vm.restoreState(savedInstanceState)

    binding.mainComposeBottom.setContent {
      val page by navi.currentScreenState()

      vm.Render { state ->
        val theme = state.theme

        SystemBars(theme)
        TickerTapeTheme(theme) {
          ProvideWindowInsets {

            // Need to have box or snackbars push up bottom bar
            Box(
                contentAlignment = Alignment.BottomCenter,
            ) {
              page?.let { p ->
                MainScreen(
                    page = p,
                    onLoadPage = { navi.navigateTo(it.asScreen()) },
                    onBottomBarHeightMeasured = { vm.handleMeasureBottomNavHeight(it) },
                )
              }
            }
          }
        }
      }
    }

    vm.handleSyncDarkTheme(this)

    navi.restoreState(savedInstanceState)
    navi.loadIfEmpty { MainPage.Home.asScreen() }
  }

  override fun onStart() {
    super.onStart()

    // Refreshes on every start and launches on initial
    lifecycleScope.launch(context = Dispatchers.Main) { tapeLauncher.requireNotNull().start() }
  }

  private fun beginWork() {
    lifecycleScope.launch(context = Dispatchers.Main) {
      alerter.requireNotNull().initOnAppStart(alarmFactory.requireNotNull())
    }
  }

  private inline fun <T : Any> retrieveFromIntent(key: String, cast: (String) -> T): T? {
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
    notificationCanceller.requireNotNull().cancelBigMoverNotification(symbol)

    WatchlistDigDialog.show(
        this,
        symbol = symbol,
        lookupSymbol = lookupSymbol,
        equityType = equityType,
        allowModifyWatchlist = false,
    )
  }

  override fun onBackPressed() {
    onBackPressedDispatcher.also { dispatcher ->
      if (dispatcher.hasEnabledCallbacks()) {
        dispatcher.onBackPressed()
      } else {
        super.onBackPressed()
      }
    }
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

  override fun getSystemService(name: String): Any? {
    return when (name) {
      // Must be defined before super.onCreate or throws npe
      MainComponent::class.java.name -> injector.requireNotNull()
      else -> super.getSystemService(name)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    viewBinding?.apply { this.mainComposeBottom.dispose() }
    viewBinding = null

    notificationCanceller = null
    alarmFactory = null
    alerter = null
    notificationCanceller = null
    navigator = null
    tapeLauncher = null
    viewModel = null
    injector = null
  }
}
