package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddScreen
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddViewModeler
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.ui.DatePickerDialog
import java.time.LocalDate
import javax.inject.Inject

internal class PositionInjector
@Inject
internal constructor(
    private val params: PositionParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: PositionAddViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ApplicationScope.retrieve(activity)
        .plusPositionComponent()
        .create(
            params = params,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: PositionAddViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(this) }
}

@Composable
internal fun PositionEntry(
    modifier: Modifier = Modifier,
    params: PositionParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { PositionInjector(params) }
  val viewModel = rememberNotNull(component.viewModel)

  val scope = rememberCoroutineScope()

  val handleDismiss by rememberUpdatedState(onDismiss)

  val handleSubmit by rememberUpdatedState {
    viewModel.handleSubmit(scope = scope) { handleDismiss() }
  }

  val handleOpenDateDialog by rememberUpdatedState { date: LocalDate? ->
    viewModel.handleOpenDateDialog(date)
  }

  val state = viewModel.state
  val dateDialog by state.datePicker.collectAsState()

  MountHooks(
      viewModel = viewModel,
  )

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = onDismiss,
  ) {
    PositionAddScreen(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        state = state,
        symbol = params.symbol,
        onClose = { handleDismiss() },
        onPriceChanged = { viewModel.handlePriceChanged(it) },
        onNumberChanged = { viewModel.handleNumberChanged(it) },
        onSubmit = { handleSubmit() },
        onDateOfPurchaseClicked = { handleOpenDateDialog(it) },
    )
  }

  dateDialog?.also { d ->
    DatePickerDialog(
        initialDate = d,
        onDateSelected = { viewModel.handleDateChanged(it) },
        onDismiss = { viewModel.handleCloseDateDialog() },
    )
  }
}
