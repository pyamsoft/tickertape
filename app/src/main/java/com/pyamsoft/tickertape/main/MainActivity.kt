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
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.height
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.android.material.appbar.AppBarLayout
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.app.AppBarActivityProvider
import com.pyamsoft.pydroid.ui.app.PYDroidActivity
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
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
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigDialog
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

internal class MainActivity :
    PYDroidActivity(),
    ToolbarActivity,
    ToolbarActivityProvider,
    AppBarActivity,
    AppBarActivityProvider {

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

  private fun navigate(page: MainPage) {
    if (navigator.requireNotNull().select(page.asScreen(), force = false)) {
      Timber.d("Loaded page: $page")
    } else {
      Timber.w("Could not push page: $page")
    }
  }

  override fun setAppBar(bar: AppBarLayout?) {}

  override fun <T> requireAppBar(func: (AppBarLayout) -> T): T {
    return func(AppBarLayout(this))
  }

  override fun <T> withAppBar(func: (AppBarLayout) -> T): T? {
    return null
  }

  override fun setToolbar(toolbar: Toolbar?) {}

  override fun <T> requireToolbar(func: (Toolbar) -> T): T {
    return func(Toolbar(this))
  }

  override fun <T> withToolbar(func: (Toolbar) -> T): T? {
    return null
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

    injector =
        Injector.obtainFromApplication<TickerComponent>(this)
            .plusMainComponent()
            .create(
                this,
                binding.mainFragmentContainerView.id,
                this,
                this,
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
            val scaffoldState = rememberScaffoldState()
            // Enforce a height for the scaffold or else it takes over the screen
            // Why do we need a scaffold instead of a box?
            // Because using a FAB with a BottomAppBar doesn't actually work unless its inside a
            // scaffold.
            //
            // yeah.
            MainScreen(
                modifier = Modifier.height(220.dp),
                scaffoldState = scaffoldState,
                page = page,
                onLoadHome = { navigate(MainPage.Home) },
                onLoadWatchList = { navigate(MainPage.WatchList) },
                onLoadPortfolio = { navigate(MainPage.Portfolio) },
                onLoadSettings = { navigate(MainPage.Settings) },
                onBottomBarHeightMeasured = { vm.handleMeasureBottomNavHeight(it) },
                onFabClicked = { Timber.d("FAB Clicked!!!") },
            ) {
              RatingScreen(
                  scaffoldState = scaffoldState,
              )
              VersionCheckScreen(
                  scaffoldState = scaffoldState,
              )
            }
          }
        }
      }
    }

    vm.handleSyncDarkTheme(this)

    navi.restore {
      if (it.select(MainPage.Home.asScreen())) {
        Timber.d("Loaded default Home screen")
      }
    }
  }

  override fun onStart() {
    super.onStart()
    lifecycleScope.launch(context = Dispatchers.Main) { tapeLauncher.requireNotNull().start() }
  }

  private fun beginWork() {
    lifecycleScope.launch(context = Dispatchers.Main) {
      alerter.requireNotNull().initOnAppStart(alarmFactory.requireNotNull())
    }
  }

  private fun handleLaunchIntent() {
    val extraKey = BigMoverNotificationData.INTENT_KEY_SYMBOL
    val launchIntent = intent
    if (launchIntent == null) {
      Timber.w("Missing launch intent")
      return
    }

    val symbolString = launchIntent.getStringExtra(extraKey)
    if (symbolString == null) {
      Timber.w("Missing launch key: $extraKey")
      return
    }

    launchIntent.removeExtra(extraKey)
    val symbol = symbolString.asSymbol()

    Timber.d("Launch intent with symbol: $symbol")
    notificationCanceller.requireNotNull().cancelBigMoverNotification(symbol)
    WatchlistDigDialog.show(this, symbol)
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
