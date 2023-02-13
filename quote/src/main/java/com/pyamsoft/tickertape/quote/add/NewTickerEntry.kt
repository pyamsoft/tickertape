package com.pyamsoft.tickertape.quote.add

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import javax.inject.Inject

internal class NewTickerInjector @Inject internal constructor() : ComposableInjector() {

  override fun onInject(activity: FragmentActivity) {}

  override fun onDispose() {}
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun NewTickerEntry(
    modifier: Modifier = Modifier,
    open: Boolean,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
  val value =
      remember(open) { if (open) ModalBottomSheetValue.Expanded else ModalBottomSheetValue.Hidden }
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = value,
      )

  LaunchedEffect(
      open,
      sheetState,
  ) {
    val hasChanged = open != sheetState.isVisible
    if (hasChanged) {
      if (open) {
        sheetState.show()
      } else {
        sheetState.hide()
      }
    }
  }

  ModalBottomSheetLayout(
      modifier = modifier,
      sheetState = sheetState,
      content = content,
      sheetContent = {
        Text(
            modifier = Modifier.clickable { onClose() },
            text = "Hello Bottom Sheet",
        )
      },
  )
}
