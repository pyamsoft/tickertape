package com.pyamsoft.tickertape.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface BottomSheetController {

  fun show()

  fun hide()
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun WrapInBottomSheet(
    sheetContent: @Composable ColumnScope.(BottomSheetController) -> Unit,
    content: @Composable (BottomSheetController) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden,
      )

  val handleOpenSheet by rememberUpdatedState {
    scope.launch(context = Dispatchers.Main) { sheetState.show() }
  }

  val handleCloseSheet by rememberUpdatedState {
    scope.launch(context = Dispatchers.Main) { sheetState.hide() }
  }

  val controller = remember {
    object : BottomSheetController {
      override fun show() {
        handleOpenSheet()
      }

      override fun hide() {
        handleCloseSheet()
      }
    }
  }

  val scrimColor = remember {
    Color.Black.copy(
        alpha = 0.4F,
    )
  }

  ModalBottomSheetLayout(
      scrimColor = scrimColor,
      sheetState = sheetState,
      sheetShape =
          MaterialTheme.shapes.medium.copy(
              bottomEnd = ZeroCornerSize,
              bottomStart = ZeroCornerSize,
          ),
      sheetContent = { sheetContent(controller) },
      content = { content(controller) },
  )
}
