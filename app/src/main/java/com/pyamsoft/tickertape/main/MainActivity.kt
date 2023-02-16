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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.installPYDroid
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.ChangeLogProvider
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.util.doOnCreate
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverNotificationData
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.InstallPYDroidExtras
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import timber.log.Timber
import javax.inject.Inject

internal class MainActivity : AppCompatActivity() {

  @JvmField @Inject internal var launcher: MainAlarmLauncher? = null
  @JvmField @Inject internal var themeViewModel: ThemeViewModeler? = null
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
      Timber.w("Cannot open Dig Dialog without symbol")
      return
    }

    val lookupSymbol =
        retrieveFromIntent(BigMoverNotificationData.INTENT_KEY_LOOKUP_SYMBOL) { it.asSymbol() }
    if (lookupSymbol == null) {
      Timber.w("Cannot open Dig Dialog without lookupSymbol")
      return
    }

    Timber.d("Launch intent with symbol: $symbol")
    launcher.requireNotNull().cancelNotifications(symbol)

    viewModel
        .requireNotNull()
        .handleOpenDig(
            scope = lifecycleScope,
            symbol = symbol,
            lookupSymbol = lookupSymbol,
        )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    stableLayoutHideNavigation()

    val component = ObjectGraph.ApplicationScope.retrieve(this).plusMainComponent().create(this)
    component.inject(this)
    ObjectGraph.ActivityScope.install(this, component)

    handleLaunchIntent()

    val tvm = themeViewModel.requireNotNull()
    val vm = viewModel.requireNotNull()
    val appName = getString(R.string.app_name)

    setContent {
      val themeState = tvm.state
      val theme by themeState.theme.collectAsState()

      val state = vm.state
      val portfolioDig by state.portfolioDigParams.collectAsState()

      val isDigging = remember(portfolioDig) { portfolioDig != null }

      TickerTapeTheme(
          theme = theme,
      ) {
        SystemBars(
            theme = theme,
            isDigging = isDigging,
        )
        InstallPYDroidExtras()

        MainEntry(
            modifier = Modifier.fillMaxSize(),
            appName = appName,
        )
      }
    }

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
    themeViewModel.requireNotNull().handleSyncDarkTheme(this)
    reportFullyDrawn()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleLaunchIntent()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    themeViewModel?.handleSyncDarkTheme(this)
  }

  override fun onDestroy() {
    super.onDestroy()

    launcher = null
    viewModel = null
    themeViewModel = null
  }
}
