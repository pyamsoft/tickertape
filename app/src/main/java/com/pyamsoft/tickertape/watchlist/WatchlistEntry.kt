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

package com.pyamsoft.tickertape.watchlist

import androidx.compose.runtime.*
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
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.add.NewTickerSheetScreen
import com.pyamsoft.tickertape.quote.add.TickerDestination
import javax.inject.Inject

class WatchlistInjector @Inject constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: WatchlistViewModeler? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  override fun onDispose() {
    viewModel = null
    imageLoader = null
  }

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusWatchlist().create().inject(this)
  }
}

@Composable
private fun MountHooks(
    viewModel: WatchlistViewModeler,
    onFabClicked: () -> Unit,
    onRefresh: () -> Unit,
) {
  val handleFabClicked by rememberUpdatedState(onFabClicked)
  val handleRefresh by rememberUpdatedState(onRefresh)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        onMainSelectionEvent = { handleFabClicked() },
    )
  }

  LifecycleEffect {
    object : DefaultLifecycleObserver {

      override fun onStart(owner: LifecycleOwner) {
        handleRefresh()
      }
    }
  }
}

@Composable
fun WatchlistEntry(
    modifier: Modifier = Modifier,
    onDigDown: (Ticker) -> Unit,
) {
  val component = rememberComposableInjector { WatchlistInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val imageLoader = rememberNotNull(component.imageLoader)

  val scope = rememberCoroutineScope()

  val handleRefresh = { force: Boolean ->
    viewModel.handleRefreshList(
        scope = scope,
        force = force,
    )
  }

  SaveStateDisposableEffect(viewModel)

  val state = viewModel.state
  val deleteTicker by state.deleteTicker.collectAsState()

  NewTickerSheetScreen(
      destination = TickerDestination.WATCHLIST,
  ) { controller ->
    MountHooks(
        viewModel = viewModel,
        onRefresh = { handleRefresh(false) },
        onFabClicked = { controller.show() },
    )

    WatchlistScreen(
        modifier = modifier,
        state = state,
        imageLoader = imageLoader,
        onSelectTicker = onDigDown,
        onRefresh = { handleRefresh(true) },
        onDeleteTicker = { viewModel.handleOpenDeleteTicker(it) },
        onSearchChanged = { viewModel.handleSearch(it) },
        onTabUpdated = { viewModel.handleSectionChanged(it) },
        onRegenerateList = { viewModel.handleRegenerateList(this) },
    )
  }

  deleteTicker?.also { ticker ->
    val params =
        remember(ticker) {
          WatchlistRemoveParams(
              symbol = ticker.symbol,
          )
        }
    WatchlistRemoveDialog(
        params = params,
        onDismiss = { viewModel.handleCloseDeleteTicker() },
    )
  }
}
