package com.pyamsoft.tickertape.quote.add

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.ui.BottomSheetController
import com.pyamsoft.tickertape.ui.BottomSheetStatus
import com.pyamsoft.tickertape.ui.WrapInBottomSheet
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

internal class NewTickerInjector
@Inject
internal constructor(
    private val destination: TickerDestination,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: NewTickerViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ApplicationScope.retrieve(activity)
        .plusNewTickerComponent()
        .create(destination)
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
internal fun NewTickerSheetScreen(
    destination: TickerDestination,
    content: @Composable (BottomSheetController) -> Unit,
) {
  WrapInBottomSheet(
      sheetContent = { controller ->
        NewTickerEntry(
            controller = controller,
            destination = destination,
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
    controller.statusFlow().collectLatest { status ->
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
    destination: TickerDestination,
    onClose: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val component = rememberComposableInjector { NewTickerInjector(destination) }
  val viewModel = rememberNotNull(component.viewModel)

  val state = viewModel.state

  val equityType by state.equityType.collectAsState()

  val handleClose by rememberUpdatedState(onClose)

  val handleCloseClicked by rememberUpdatedState {
    if (equityType == null) {
      handleClose()
    } else {
      viewModel.handleClearEquityType()
    }
  }

  MountHooks(
      viewModel = viewModel,
      controller = controller,
  )

  SaveStateDisposableEffect(viewModel)

  BackHandler(
      onBack = { handleCloseClicked() },
  )
  NewTickerScreen(
      modifier = modifier,
      state = state,
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
