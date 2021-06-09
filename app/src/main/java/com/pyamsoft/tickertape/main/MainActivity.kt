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
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.ui.Injector
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
import com.pyamsoft.tickertape.setting.SettingsFragment
import com.pyamsoft.tickertape.watchlist.WatchlistFragment
import javax.inject.Inject

internal class MainActivity : ChangeLogActivity(), UiController<MainControllerEvent> {

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

    private var rootBinding: LayoutCoordinatorBinding? = null
    private var stateSaver: StateSaver? = null

    @JvmField
    @Inject
    internal var factory: MainViewModel.Factory? = null
    private val viewModel by fromViewModelFactory<MainViewModel> {
        createSavedStateViewModelFactory(factory)
    }

    @JvmField
    @Inject
    internal var container: MainContainer? = null

    @JvmField
    @Inject
    internal var bottomBar: MainBar? = null

    @JvmField
    @Inject
    internal var addNew: MainBarAdd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TickerTape)
        super.onCreate(savedInstanceState)
        val binding = LayoutCoordinatorBinding.inflate(layoutInflater).apply { rootBinding = this }
        setContentView(binding.root)

        Injector.obtainFromApplication<TickerComponent>(this)
            .plusMainComponent()
            .create(this, this, this, binding.layoutCoordinator, this)
            .inject(this)

        stableLayoutHideNavigation()

        inflateComponents(savedInstanceState)
    }

    private fun inflateComponents(savedInstanceState: Bundle?) {
        val container = requireNotNull(container)
        val bottomBar = requireNotNull(bottomBar)
        val addNew = requireNotNull(addNew)

        stateSaver =
            createComponent(
                savedInstanceState,
                this,
                viewModel,
                this,
                container,
                container,
                bottomBar,
                addNew,
            ) {
                return@createComponent when (it) {
                    is MainViewEvent.BottomBarMeasured -> viewModel.handleConsumeBottomBarHeight(it.height)
                    is MainViewEvent.FabCradleVisibility -> viewModel.handlePublishFabVisibility(it.visible)
                    is MainViewEvent.OpenWatchList ->
                        viewModel.handleSelectPage(MainPage.WatchList, force = false)
                    is MainViewEvent.OpenSettings ->
                        viewModel.handleSelectPage(MainPage.Settings, force = false)
                }
            }

        val existingFragment = supportFragmentManager.findFragmentById(fragmentContainerId)
        if (savedInstanceState == null || existingFragment == null) {
            viewModel.handleLoadDefaultPage()
        }
    }

    override fun onControllerEvent(event: MainControllerEvent) {
        return when (event) {
            is MainControllerEvent.PushPage -> handlePushPage(
                event.newPage,
                event.oldPage,
                event.force
            )
        }
    }

    private fun handlePushPage(newPage: MainPage, oldPage: MainPage?, force: Boolean) {
        val fragment: Fragment
        val tag: String
        when (newPage) {
            is MainPage.Settings -> {
                fragment = SettingsFragment.newInstance()
                tag = SettingsFragment.TAG
            }
            is MainPage.WatchList -> {
                fragment = WatchlistFragment.newInstance()
                tag = WatchlistFragment.TAG
            }
        }

        supportFragmentManager.commitNow(this) {
            replace(fragmentContainerId, fragment, tag)
        }
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

        rootBinding = null
        container = null
        bottomBar = null
        addNew = null
    }
}
