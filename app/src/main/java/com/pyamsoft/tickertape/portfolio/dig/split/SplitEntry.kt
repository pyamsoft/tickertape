package com.pyamsoft.tickertape.portfolio.dig.split

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.portfolio.dig.splits.add.SplitAddScreen
import com.pyamsoft.tickertape.portfolio.dig.splits.add.SplitAddViewModeler
import com.pyamsoft.tickertape.quote.dig.SplitParams
import javax.inject.Inject

internal class SplitInjector
@Inject
internal constructor(
    private val params: SplitParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: SplitAddViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ApplicationScope.retrieve(activity)
        .plusSplitComponent()
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
    viewModel: SplitAddViewModeler,
) {
  LaunchedEffect(viewModel) { viewModel.bind(this) }
}

@Composable
internal fun SplitEntry(
    modifier: Modifier = Modifier,
    params: SplitParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { SplitInjector(params) }
  val viewModel = rememberNotNull(component.viewModel)

  val scope = rememberCoroutineScope()

  MountHooks(
      viewModel = viewModel,
  )

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = onDismiss,
  ) {
    SplitAddScreen(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        state = viewModel.state,
        symbol = params.symbol,
        onClose = onDismiss,
        onPreSplitCountChanged = { viewModel.handlePreSplitShareCountChanged(it) },
        onPostSplitCountChanged = { viewModel.handlePostSplitShareCountChanged(it) },
        onSubmit = { viewModel.handleSubmit(scope = scope) { onDismiss() } },
        onSplitDateClicked = {
          viewModel.handleOpenDateDialog { splitId ->
            // TODO open date dialog
          }
        },
    )
  }
}
