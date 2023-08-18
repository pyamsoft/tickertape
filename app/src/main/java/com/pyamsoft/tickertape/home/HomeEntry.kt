/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.setting.SettingsDialog
import javax.inject.Inject

class HomeInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: HomeViewModeler? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  override fun onDispose() {
    viewModel = null
    imageLoader = null
  }

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusHome().create().inject(this)
  }
}

@Composable
fun HomeEntry(
    modifier: Modifier = Modifier,
    appName: String,
    onDig: (Ticker) -> Unit,
) {
  val component = rememberComposableInjector { HomeInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val imageLoader = rememberNotNull(component.imageLoader)

  val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()

  HomeScreen(
      modifier = modifier,
      state = viewModel,
      appName = appName,
      imageLoader = imageLoader,
      onSettingsClicked = { viewModel.handleOpenSettings() },
      onChartClicked = { onDig(it) },
      onRefreshUndervaluedGrowth = {
        viewModel.handleFetchUndervaluedGrowth(
            scope = this,
            force = false,
        )
      },
      onRefreshTrending = {
        viewModel.handleFetchTrending(
            scope = this,
            force = false,
        )
      },
      onRefreshPortfolio = {
        viewModel.handleFetchPortfolio(
            scope = this,
            force = false,
        )
      },
      onRefreshMostShorted = {
        viewModel.handleFetchMostShorted(
            scope = this,
            force = false,
        )
      },
      onRefreshLosers = {
        viewModel.handleFetchLosers(
            scope = this,
            force = false,
        )
      },
      onRefreshIndexes = {
        viewModel.handleFetchIndexes(
            scope = this,
            force = false,
        )
      },
      onRefreshGrowthTech = {
        viewModel.handleFetchGrowthTech(
            scope = this,
            force = false,
        )
      },
      onRefreshGainers = {
        viewModel.handleFetchGainers(
            scope = this,
            force = false,
        )
      },
      onRefreshMostActive = {
        viewModel.handleFetchMostActive(
            scope = this,
            force = false,
        )
      },
  )

  if (isSettingsOpen) {
    SettingsDialog(
        modifier = Modifier.fillUpToPortraitSize(),
        onDismiss = { viewModel.handleCloseSettings() },
    )
  }
}
