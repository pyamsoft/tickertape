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

package com.pyamsoft.tickertape.quote.add

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.ui.BottomSheetController
import com.pyamsoft.tickertape.ui.BottomSheetStatus
import com.pyamsoft.tickertape.ui.WrapInBottomSheet
import javax.inject.Inject

internal class NewTickerInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: NewTickerViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ApplicationScope.retrieve(activity).plusNewTickerComponent().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
internal fun NewTickerSheetScreen(
    content: @Composable (BottomSheetController) -> Unit,
) {
  WrapInBottomSheet(
      sheetContent = { controller ->
        NewTickerEntry(
            controller = controller,
            onClose = { controller.hide() },
        )
      },
  ) { controller ->
    content(controller)
  }
}

@Composable
private fun MountHooks(
    viewModel: NewTickerViewModeler,
    controller: BottomSheetController,
) {
  LaunchedEffect(
      viewModel,
      controller,
  ) {
    controller.statusFlow().collect { status ->
      if (status == BottomSheetStatus.CLOSED) {
        viewModel.handleDismiss()
      }
    }
  }
}

@Composable
private fun NewTickerEntry(
    modifier: Modifier = Modifier,
    controller: BottomSheetController,
    onClose: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val component = rememberComposableInjector { NewTickerInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  val equityType by viewModel.equityType.collectAsState()

  val handleClose by rememberUpdatedState(onClose)

  val handleCloseClicked by rememberUpdatedState {
    if (equityType == null) {
      handleClose()
    } else {
      viewModel.handleClearEquityType()
    }
  }

  val sheetStatus = controller.status
  val isOpen = remember(sheetStatus) { sheetStatus == BottomSheetStatus.OPEN }

  MountHooks(
      viewModel = viewModel,
      controller = controller,
  )

  SaveStateDisposableEffect(viewModel)

  BackHandler(
      enabled = isOpen,
      onBack = { handleCloseClicked() },
  )
  NewTickerScreen(
      modifier = modifier,
      state = viewModel,
      onClose = { handleCloseClicked() },
      onTypeSelected = { viewModel.handleEquityTypeSelected(it) },
      onSymbolChanged = { viewModel.handleSymbolChanged(it) },
      onSubmit = { viewModel.handleSubmit(scope = scope) },
      onClear = { viewModel.handleClear() },
      onTradeSideSelected = { viewModel.handleTradeSideChanged(it) },
      onResultsDismissed = { viewModel.handleSearchResultsDismissed() },
      onOptionTypeSlected = { viewModel.handleOptionType(it) },
      onStrikeSelected = { viewModel.handleOptionStrikePrice(it) },
      onSearchResultSelected = {
        viewModel.handleSearchResultSelected(
            scope = scope,
            result = it,
        )
      },
      onExpirationDateSelected = {
        viewModel.handleOptionExpirationDate(
            scope = scope,
            date = it,
        )
      },
      onAfterSymbolChanged = {
        viewModel.handleAfterSymbolChanged(
            scope = scope,
            symbol = it,
        )
      },
  )
}
