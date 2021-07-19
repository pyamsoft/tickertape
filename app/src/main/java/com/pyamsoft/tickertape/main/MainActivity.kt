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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.app.AppBarActivityProvider
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.changelog.ChangeLogActivity
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.databinding.LayoutCoordinatorBinding
import com.pyamsoft.pydroid.ui.util.commitNow
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.tickertape.BuildConfig
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.home.HomeFragment
import com.pyamsoft.tickertape.initOnAppStart
import com.pyamsoft.tickertape.portfolio.PortfolioFragment
import com.pyamsoft.tickertape.portfolio.manage.position.add.BasePositionsAddComponent
import com.pyamsoft.tickertape.setting.SettingsFragment
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.watchlist.WatchlistFragment
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MainActivity :
    ChangeLogActivity(), UiController<MainControllerEvent>, AppBarActivity, AppBarActivityProvider {

  override val checkForUpdates = false

  override val applicationIcon = R.mipmap.ic_launcher

  override val changelog: ChangeLogBuilder = buildChangeLog {}

  override val versionName = BuildConfig.VERSION_NAME

  override val fragmentContainerId: Int
    get() = requireNotNull(container).id()

  override val snackbarRoot: ViewGroup
    get() {
      return requireNotNull(rootBinding).layoutCoordinator
    }

  private var basePositionsAddComponent: BasePositionsAddComponent? = null

  private var rootBinding: LayoutCoordinatorBinding? = null
  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var tapeLauncher: TapeLauncher? = null

  @JvmField @Inject internal var factory: MainViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<MainViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  @JvmField @Inject internal var container: MainContainer? = null

  @JvmField @Inject internal var bottomBar: MainBar? = null

  @JvmField @Inject internal var addNew: MainBarAdd? = null

  @JvmField @Inject internal var toolbar: MainToolbar? = null

  @JvmField @Inject internal var alerter: Alerter? = null

  @JvmField @Inject internal var alarmFactory: AlarmFactory? = null

  private var capturedAppBar: AppBarLayout? = null

  override fun setAppBar(bar: AppBarLayout?) {
    capturedAppBar = bar
  }

  override fun requireAppBar(func: (AppBarLayout) -> Unit) {
    requireNotNull(capturedAppBar).let(func)
  }

  override fun withAppBar(func: (AppBarLayout) -> Unit) {
    capturedAppBar?.let(func)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_TickerTape)
    super.onCreate(savedInstanceState)
    val binding = LayoutCoordinatorBinding.inflate(layoutInflater).apply { rootBinding = this }
    setContentView(binding.root)

    Injector.obtainFromApplication<TickerComponent>(this)
      .also {  component ->
        basePositionsAddComponent = component.plusPositionAddComponent().create()

        component.plusMainComponent()
        .create(this, this, this, binding.layoutCoordinator, this, this)
        .inject(this)
      }

    stableLayoutHideNavigation()

    inflateComponents(savedInstanceState)
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
            toolbar.requireNotNull()) {
          return@createComponent when (it) {
            is MainViewEvent.BottomBarMeasured -> viewModel.handleConsumeBottomBarHeight(it.height)
            is MainViewEvent.OpenHome -> viewModel.handleSelectPage(MainPage.Home, force = false)
            is MainViewEvent.OpenPortfolio ->
                viewModel.handleSelectPage(MainPage.Portfolio, force = false)
            is MainViewEvent.OpenWatchList ->
                viewModel.handleSelectPage(MainPage.WatchList, force = false)
            is MainViewEvent.OpenSettings ->
                viewModel.handleSelectPage(MainPage.Settings, force = false)
            is MainViewEvent.AddRequest -> viewModel.handleAddNewRequest()
          }
        }

    val existingFragment = supportFragmentManager.findFragmentById(fragmentContainerId)
    if (savedInstanceState == null || existingFragment == null) {
      viewModel.handleLoadDefaultPage()
    }

    beginWork()
  }

  override fun onStart() {
    super.onStart()
    requireNotNull(tapeLauncher).start()
  }

  private fun beginWork() {
    lifecycleScope.launch(context = Dispatchers.Default) {
      requireNotNull(alerter).initOnAppStart(requireNotNull(alarmFactory))
    }
  }

  override fun getSystemService(name: String): Any? {
    return when (name) {
      BasePositionsAddComponent::class.java.name -> basePositionsAddComponent.requireNotNull()
      else -> super.getSystemService(name)
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
      is MainPage.Settings -> {
        fragment = SettingsFragment.newInstance()
        tag = SettingsFragment.TAG
      }
      is MainPage.WatchList -> {
        fragment = WatchlistFragment.newInstance()
        tag = WatchlistFragment.TAG
      }
      is MainPage.Portfolio -> {
        fragment = PortfolioFragment.newInstance()
        tag = PortfolioFragment.TAG
      }
    }.requireNotNull()

    supportFragmentManager.commitNow(this) { replace(fragmentContainerId, fragment, tag) }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
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
    capturedAppBar = null

    rootBinding = null
    container = null
    bottomBar = null
    addNew = null
    toolbar = null
  }
}
