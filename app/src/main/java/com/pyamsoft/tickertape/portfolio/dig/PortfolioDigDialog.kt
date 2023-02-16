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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberActivity
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.position.PositionDialog
import com.pyamsoft.tickertape.portfolio.dig.split.SplitDialog
import com.pyamsoft.tickertape.quote.Ticker
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
            symbol = params.symbol,
            lookupSymbol = params.lookupSymbol,
            holdingId = params.holdingId,
            holdingType = params.holdingType,
            tradeSide = params.holdingSide,
            currentPrice = params.currentPrice,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
    imageLoader = null
  }
}

@Composable
private fun MountHooks(
    viewModel: PortfolioDigViewModeler,
) {
  LaunchedEffect(viewModel) { viewModel.bind(this) }

  LaunchedEffect(viewModel) {
    viewModel.handleLoadTicker(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun PortfolioDigDialog(
    modifier: Modifier = Modifier,
    params: PortfolioDigParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { PortfolioDigInjector(params) }

  val viewModel = rememberNotNull(component.viewModel)
  val imageLoader = rememberNotNull(component.imageLoader)

  val scope = rememberCoroutineScope()
  val activity = rememberActivity()

  val handlePositionAdd by rememberUpdatedState {
    PositionDialog.create(
        activity = activity,
        symbol = params.symbol,
        holdingId = params.holdingId,
        holdingType = params.holdingType,
    )
  }

  val handlePositionUpdate by rememberUpdatedState { position: DbPosition ->
    PositionDialog.update(
        activity = activity,
        symbol = params.symbol,
        holdingId = params.holdingId,
        holdingType = params.holdingType,
        existingPositionId = position.id,
    )
  }

  val handleSplitAdd by rememberUpdatedState {
    SplitDialog.create(
        activity = activity,
        symbol = params.symbol,
        holdingId = params.holdingId,
    )
  }

  val handleSplitUpdate by rememberUpdatedState { split: DbSplit ->
    SplitDialog.update(
        activity = activity,
        symbol = params.symbol,
        holdingId = params.holdingId,
        existingSplitId = split.id,
    )
  }

  val handleRecommendationClicked by rememberUpdatedState { ticker: Ticker ->
    // TODO
  }

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  PortfolioDigScreen(
      modifier = modifier,
      state = viewModel.state,
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
      onPositionAdd = { handlePositionAdd() },
      onPositionDelete = {
        viewModel.handleDeletePosition(
            scope = scope,
            position = it,
        )
      },
      onPositionUpdate = { handlePositionUpdate(it) },
      onSplitAdd = { handleSplitAdd() },
      onSplitDeleted = {
        viewModel.handleDeleteSplit(
            scope = scope,
            split = it,
        )
      },
      onSplitUpdated = { handleSplitUpdate(it) },
      onRecClick = { handleRecommendationClicked(it) },
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
}
