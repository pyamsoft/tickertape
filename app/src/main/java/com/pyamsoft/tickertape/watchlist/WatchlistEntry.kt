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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.LifecycleEffect
import com.pyamsoft.pydroid.ui.util.rememberActivity
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.quote.add.NewTickerSheet
import com.pyamsoft.tickertape.quote.add.TickerDestination
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import javax.inject.Inject
import timber.log.Timber

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
  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        onMainSelectionEvent = { onFabClicked() },
    )
  }

  LifecycleEffect {
    object : DefaultLifecycleObserver {

      override fun onStart(owner: LifecycleOwner) {
        onRefresh()
      }
    }
  }
}

@Composable
fun WatchlistEntry(
    modifier: Modifier = Modifier,
) {
  val component = rememberComposableInjector { WatchlistInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val imageLoader = rememberNotNull(component.imageLoader)

  val activity = rememberActivity()
  val scope = rememberCoroutineScope()

  // Declare here since it is used in mount hook
  val handleRefresh = { force: Boolean ->
    viewModel.handleRefreshList(
        scope = scope,
        force = force,
    )
  }

  MountHooks(
      viewModel = viewModel,
      onRefresh = { handleRefresh(false) },
      onFabClicked = {
        // TODO move away from sheet
        NewTickerSheet.show(
            activity,
            TickerDestination.WATCHLIST,
        )
      },
  )

  SaveStateDisposableEffect(viewModel)

  WatchlistScreen(
      modifier = modifier,
      state = viewModel.state,
      imageLoader = imageLoader,
      onRefresh = { handleRefresh(true) },
      onDeleteTicker = { ticker ->
        // TODO move away from dialog fragment
        WatchlistRemoveDialog.show(
            activity,
            symbol = ticker.symbol,
        )
      },
      onSearchChanged = { viewModel.handleSearch(it) },
      onTabUpdated = { viewModel.handleSectionChanged(it) },
      onSelectTicker = { ticker ->
        val quote = ticker.quote
        if (quote == null) {
          Timber.w("Can't show dig dialog, missing quote: ${ticker.symbol}")
          return@WatchlistScreen
        }

        val equityType = quote.type
        val lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol else quote.symbol
        // TODO open drilldown
      },
      onRegenerateList = { viewModel.handleRegenerateList(this) },
  )
}
