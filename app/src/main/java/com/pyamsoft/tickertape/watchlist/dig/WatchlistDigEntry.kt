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

package com.pyamsoft.tickertape.watchlist.dig

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.LifecycleEffect
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.quote.screen.WatchlistDigParams
import javax.inject.Inject

internal data class WatchlistDigInjector
@Inject
internal constructor(
    private val params: WatchlistDigParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: WatchlistDigViewModeler? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  override fun onDispose() {
    viewModel = null
    imageLoader = null
  }

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusWatchlistDig()
        .create(
            params.symbol,
            params.lookupSymbol,
            params.equityType,
        )
        .inject(this)
  }
}

@Composable
private fun MountHooks(
    onRefresh: () -> Unit,
) {
  val handleRefresh by rememberUpdatedState(onRefresh)

  LifecycleEffect {
    object : DefaultLifecycleObserver {
      override fun onStart(owner: LifecycleOwner) {
        handleRefresh()
      }
    }
  }
}

@Composable
internal fun WatchlistDigEntry(
    modifier: Modifier = Modifier,
    params: WatchlistDigParams,
    onGoBack: () -> Unit,
) {
  val component = rememberComposableInjector { WatchlistDigInjector(params) }
  val viewModel = rememberNotNull(component.viewModel)
  val imageLoader = rememberNotNull(component.imageLoader)

  val scope = rememberCoroutineScope()

  val handleRefresh = { force: Boolean ->
    viewModel.handleLoadTicker(
        scope = scope,
        force = force,
    )
  }

  val state = viewModel.state

  val recommendation by state.digRecommendation.collectAsState()

  SaveStateDisposableEffect(viewModel)

  Crossfade(
      targetState = recommendation,
  ) { rec ->
    if (rec == null) {
      MountHooks(
          onRefresh = { handleRefresh(false) },
      )
      BackHandler(
          onBack = onGoBack,
      )
      WatchlistDigScreen(
          modifier = modifier,
          state = state,
          imageLoader = imageLoader,
          onClose = onGoBack,
          onChartScrub = { viewModel.handleChartDateScrubbed(it) },
          onChartRangeSelected = { range ->
            viewModel.handleChartRangeSelected(
                scope = scope,
                range = range,
            )
          },
          onModifyWatchlist = { viewModel.handleModifyWatchlist(scope = scope) },
          onRefresh = { handleRefresh(true) },
          onTabUpdated = { section ->
            viewModel.handleTabUpdated(
                scope = scope,
                section = section,
            )
          },
          onRecClick = { viewModel.handleOpenRecommendation(it) },
          onOptionSectionChanged = { viewModel.handleOptionsSectionChanged(it) },
          onOptionExpirationDateChanged = { date ->
            viewModel.handleOptionsExpirationDateChanged(
                scope = scope,
                date = date,
            )
          },
          onAddPriceAlert = {
            // TODO add alert
          },
          onUpdatePriceAlert = { alert ->
            // TODO update alert
          },
          onDeletePriceAlert = { alert ->
            // TODO delete alert
          },
      )
    } else {
      WatchlistDigEntry(
          modifier = modifier,
          params = rec,
          onGoBack = { viewModel.handleCloseRecommendation() },
      )
    }
  }
}
