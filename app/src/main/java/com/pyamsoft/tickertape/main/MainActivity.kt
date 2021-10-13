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
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.asFactory
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.app.AppBarActivityProvider
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.changelog.ChangeLogActivity
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.databinding.LayoutCoordinatorBinding
import com.pyamsoft.pydroid.ui.util.commitNow
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.tickertape.BuildConfig
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.notification.BigMoverNotificationData
import com.pyamsoft.tickertape.alert.notification.NotificationCanceller
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.home.HomeFragment
import com.pyamsoft.tickertape.initOnAppStart
import com.pyamsoft.tickertape.notification.NotificationFragment
import com.pyamsoft.tickertape.portfolio.PortfolioFragment
import com.pyamsoft.tickertape.setting.SettingsDialog
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.watchlist.WatchlistFragment
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigDialog
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

internal class MainActivity :
    ChangeLogActivity(),
    UiController<MainControllerEvent>,
    AppBarActivity,
    AppBarActivityProvider,
    ToolbarActivity,
    ToolbarActivityProvider {

  override val checkForUpdates = false

  override val applicationIcon = R.mipmap.ic_launcher

  override val changelog: ChangeLogBuilder = buildChangeLog {}

  override val versionName = BuildConfig.VERSION_NAME

  private val fragmentContainerId: Int
    get() = container.requireNotNull().id()

  override val snackbarRoot: ViewGroup
    get() {
      return rootBinding.requireNotNull().layoutCoordinator
    }

  private var rootBinding: LayoutCoordinatorBinding? = null
  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var notificationCanceller: NotificationCanceller? = null

  @JvmField @Inject internal var tapeLauncher: TapeLauncher? = null

  @JvmField @Inject internal var factory: MainViewModel.Factory? = null
  private val viewModel by viewModels<MainViewModel> { factory.requireNotNull().asFactory(this) }

  @JvmField @Inject internal var container: MainContainer? = null

  @JvmField @Inject internal var bottomBar: MainBar? = null

  @JvmField @Inject internal var addNew: MainBarAdd? = null

  @JvmField @Inject internal var toolbar: MainToolbar? = null

  @JvmField @Inject internal var alerter: Alerter? = null

  @JvmField @Inject internal var alarmFactory: AlarmFactory? = null

  private var capturedAppBar: AppBarLayout? = null

  private var capturedToolbar: Toolbar? = null

  override fun setAppBar(bar: AppBarLayout?) {
    capturedAppBar = bar
  }

  override fun <T> requireAppBar(func: (AppBarLayout) -> T): T {
    return capturedAppBar.requireNotNull().let(func)
  }

  override fun <T> withAppBar(func: (AppBarLayout) -> T): T? {
    return capturedAppBar?.let(func)
  }

  override fun setToolbar(toolbar: Toolbar?) {
    capturedToolbar = toolbar
  }

  override fun <T> requireToolbar(func: (Toolbar) -> T): T {
    return capturedToolbar.requireNotNull().let(func)
  }

  override fun <T> withToolbar(func: (Toolbar) -> T): T? {
    return capturedToolbar?.let(func)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_TickerTape)
    super.onCreate(savedInstanceState)
    val binding = LayoutCoordinatorBinding.inflate(layoutInflater).apply { rootBinding = this }
    setContentView(binding.root)

    Injector.obtainFromApplication<TickerComponent>(this)
        .plusMainComponent()
        .create(this, this, this, binding.layoutCoordinator, this, this)
        .inject(this)

    lifecycleScope

    stableLayoutHideNavigation()

    inflateComponents(savedInstanceState)
    beginWork()
    handleLaunchIntent()

    viewModel.handleLoadDefaultPage()
  }

  private fun inflateComponents(savedInstanceState: Bundle?) {
    stateSaver =
        createComponent(
            savedInstanceState,
            this,
            viewModel,
            this,
            container.requireNotNull(),
            bottomBar.requireNotNull(),
            addNew.requireNotNull(),
            toolbar.requireNotNull(),
        ) {
          return@createComponent when (it) {
            is MainViewEvent.BottomBarMeasured -> viewModel.handleConsumeBottomBarHeight(it.height)
            is MainViewEvent.TopBarMeasured -> viewModel.handleConsumeTopBarHeight(it.height)
            is MainViewEvent.OpenHome -> viewModel.handleSelectPage(MainPage.Home, force = false)
            is MainViewEvent.OpenPortfolio ->
                viewModel.handleSelectPage(MainPage.Portfolio, force = false)
            is MainViewEvent.OpenWatchList ->
                viewModel.handleSelectPage(MainPage.WatchList, force = false)
            is MainViewEvent.OpenNotifications ->
                viewModel.handleSelectPage(MainPage.Notifications, force = false)
            is MainViewEvent.AddRequest -> viewModel.handleAddNewRequest()
            is MainViewEvent.OpenAdd -> viewModel.handleOpenAdd(it.type, it.side)
            is MainViewEvent.OpenSettings -> handleOpenSettings()
            is MainViewEvent.StopAdd -> viewModel.handleStopAdd()
          }
        }
  }

  override fun onStart() {
    super.onStart()
    lifecycleScope.launch(context = Dispatchers.Default) { tapeLauncher.requireNotNull().start() }
  }

  private fun handleOpenSettings() {
    SettingsDialog.newInstance().show(this, SettingsDialog.TAG)
  }

  private fun beginWork() {
    lifecycleScope.launch(context = Dispatchers.Default) {
      alerter.requireNotNull().initOnAppStart(alarmFactory.requireNotNull())
    }
  }

  override fun onControllerEvent(event: MainControllerEvent) {
    return when (event) {
      is MainControllerEvent.PushPage -> handlePushPage(event.newPage, event.oldPage, event.force)
    }
  }

  private fun handlePushPage(newPage: MainPage, oldPage: MainPage?, force: Boolean) {
    val fragment: Fragment
    val tag: String
    when (newPage) {
      is MainPage.Home -> {
        fragment = HomeFragment.newInstance()
        tag = HomeFragment.TAG
      }
      is MainPage.Notifications -> {
        fragment = NotificationFragment.newInstance()
        tag = NotificationFragment.TAG
      }
      is MainPage.WatchList -> {
        fragment = WatchlistFragment.newInstance()
        tag = WatchlistFragment.TAG
      }
      is MainPage.Portfolio -> {
        fragment = PortfolioFragment.newInstance()
        tag = PortfolioFragment.TAG
      }
    }

    supportFragmentManager.commitNow(this) { replace(fragmentContainerId, fragment, tag) }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleLaunchIntent()
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
    WatchlistDigDialog.newInstance(symbol).show(this, WatchlistDigDialog.TAG)
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

  override fun onSaveInstanceState(outState: Bundle) {
    stateSaver?.saveState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    stateSaver = null
    factory = null
    notificationCanceller = null

    capturedAppBar = null
    capturedToolbar = null
    rootBinding = null

    container = null
    bottomBar = null
    addNew = null
    toolbar = null
  }
}
