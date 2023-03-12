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

package com.pyamsoft.tickertape.portfolio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.LifecycleEffect
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.quote.add.NewTickerSheetScreen
import com.pyamsoft.tickertape.ui.BottomSheetController
import javax.inject.Inject

internal class PortfolioInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: PortfolioViewModeler? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  override fun onDispose() {
    viewModel = null
    imageLoader = null
  }

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusPortfolio().create().inject(this)
  }
}

@Composable
private fun MountHooks(
    viewModel: PortfolioViewModeler,
    controller: BottomSheetController,
) {
  SaveStateDisposableEffect(viewModel)

  val handleShowController by rememberUpdatedState { controller.show() }
  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        onMainSelectionEvent = { handleShowController() },
    )
  }

  LifecycleEffect {
    object : DefaultLifecycleObserver {

      override fun onStart(owner: LifecycleOwner) {
        viewModel.handleRefreshList(
            scope = owner.lifecycleScope,
            force = false,
        )
      }
    }
  }
}

@Composable
internal fun PortfolioEntry(
    modifier: Modifier = Modifier,
    onDig: (PortfolioStock) -> Unit,
) {
  NewTickerSheetScreen { controller ->
    val component = rememberComposableInjector { PortfolioInjector() }
    val viewModel = rememberNotNull(component.viewModel)
    val imageLoader = rememberNotNull(component.imageLoader)

    val scope = rememberCoroutineScope()

    val state = viewModel.state
    val removeDialog by state.remove.collectAsState()

    MountHooks(
        viewModel = viewModel,
        controller = controller,
    )

    PortfolioScreen(
        modifier = modifier,
        state = state,
        imageLoader = imageLoader,
        onRefresh = {
          viewModel.handleRefreshList(
              scope = scope,
              force = true,
          )
        },
        onSelect = { onDig(it) },
        onDelete = { viewModel.handleOpenDelete(it) },
        onSearchChanged = { viewModel.handleSearch(it) },
        onTabUpdated = { viewModel.handleSectionChanged(it) },
        onRegenerateList = { viewModel.handleRegenerateList(scope = scope) },
        onHoldingDeleteFinalized = { viewModel.handleHoldingDeleteFinal(scope = scope) },
        onHoldingRestored = { viewModel.handleRestoreDeletedHolding(scope = scope) },
    )

    removeDialog?.also { r ->
      PortfolioRemoveDialog(
          params = r,
          onDismiss = { viewModel.handleCloseDelete() },
      )
    }
  }
}
