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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.portfolio.dig.position.PositionEntry
import com.pyamsoft.tickertape.portfolio.dig.split.SplitEntry
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import javax.inject.Inject

internal class PortfolioDigInjector
@Inject
internal constructor(
    private val params: PortfolioDigParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: PortfolioDigViewModeler? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusPortfolioDig()
        .create(
            params = params,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
    imageLoader = null
  }
}

@Composable
internal fun PortfolioDigEntry(
    modifier: Modifier = Modifier,
    params: PortfolioDigParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { PortfolioDigInjector(params) }

  val viewModel = rememberNotNull(component.viewModel)

  val state = viewModel.state
  val recDig by state.recommendedDig.collectAsState()

  recDig.also { rec ->
    if (rec == null) {
      PortfolioDigContent(
          modifier = modifier,
          params = params,
          component = component,
          onDismiss = onDismiss,
      )
    } else {
      PortfolioDigEntry(
          modifier = modifier,
          params = rec,
          onDismiss = { viewModel.handleCloseRec() },
      )
    }
  }
}

@Composable
private fun MountHooks(
    viewModel: PortfolioDigViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(this) }

  LaunchedEffect(viewModel) {
    viewModel.handleLoadTicker(
        scope = this,
        force = false,
    )
  }
}

@Composable
private fun PortfolioDigContent(
    modifier: Modifier = Modifier,
    params: PortfolioDigParams,
    component: PortfolioDigInjector,
    onDismiss: () -> Unit,
) {
  val viewModel = rememberNotNull(component.viewModel)
  val imageLoader = rememberNotNull(component.imageLoader)

  val scope = rememberCoroutineScope()

  val state = viewModel.state
  val positionDialog by state.positionDialog.collectAsState()
  val splitDialog by state.splitDialog.collectAsState()

  BackHandler(
      onBack = onDismiss,
  )

  MountHooks(
      viewModel = viewModel,
  )

  PortfolioDigScreen(
      modifier = modifier,
      state = state,
      imageLoader = imageLoader,
      onClose = onDismiss,
      onChartScrub = { viewModel.handleChartDateScrubbed(it) },
      onChartRangeSelected = {
        viewModel.handleChartRangeSelected(
            scope = scope,
            range = it,
        )
      },
      onTabUpdated = {
        viewModel.handleTabUpdated(
            scope = scope,
            section = it,
        )
      },
      onRefresh = {
        viewModel.handleLoadTicker(
            scope = scope,
            force = true,
        )
      },
      onPositionAdd = { viewModel.handleOpenPosition(params, it) },
      onPositionUpdate = { p, h -> viewModel.handleOpenPosition(params, h, p) },
      onPositionDelete = {
        viewModel.handleDeletePosition(
            scope = scope,
            position = it,
        )
      },
      onSplitAdd = { viewModel.handleOpenSplit(params, it) },
      onSplitUpdated = { s, h -> viewModel.handleOpenSplit(params, h, s) },
      onSplitDeleted = {
        viewModel.handleDeleteSplit(
            scope = scope,
            split = it,
        )
      },
      onRecClick = { viewModel.handleRecClicked(it) },
      onOptionSectionChanged = { viewModel.handleOptionsSectionChanged(it) },
      onOptionExpirationDateChanged = {
        viewModel.handleOptionsExpirationDateChanged(
            scope = scope,
            date = it,
        )
      },
      onAddPriceAlert = {
        // TODO
      },
      onUpdatePriceAlert = {
        // TODO
      },
      onDeletePriceAlert = {
        // TODO
      },
  )

  splitDialog?.also { s ->
    SplitEntry(
        params = s,
        onDismiss = { viewModel.handleCloseSplit() },
    )
  }

  positionDialog?.also { p ->
    PositionEntry(
        params = p,
        onDismiss = { viewModel.handleClosePosition() },
    )
  }
}
