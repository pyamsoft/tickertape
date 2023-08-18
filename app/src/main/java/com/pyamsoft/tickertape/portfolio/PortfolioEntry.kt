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

package com.pyamsoft.tickertape.portfolio

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import coil.ImageLoader
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.LifecycleEventEffect
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

  override fun onInject(activity: ComponentActivity) {
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

  val scope = rememberCoroutineScope()
  LifecycleEventEffect(event = Lifecycle.Event.ON_START) {
    viewModel.handleRefreshList(
        scope = scope,
        force = false,
    )
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

    val removeDialog by viewModel.remove.collectAsStateWithLifecycle()

    MountHooks(
        viewModel = viewModel,
        controller = controller,
    )

    PortfolioScreen(
        modifier = modifier,
        state = viewModel,
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
        onHoldingDeleteFinalized = { viewModel.handleHoldingDeleteFinal() },
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
