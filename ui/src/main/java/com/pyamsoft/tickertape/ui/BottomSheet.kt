package com.pyamsoft.tickertape.ui

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface BottomSheetController {

  fun show()

  fun hide()

  @CheckResult fun statusFlow(): Flow<BottomSheetStatus>
}

enum class BottomSheetStatus {
  OPEN,
  HALF,
  CLOSED
}

@CheckResult
@OptIn(ExperimentalMaterialApi::class)
private fun ModalBottomSheetState.toStatusFlow(): Flow<BottomSheetStatus> {
  return snapshotFlow { this.currentValue }
      .map { value ->
        when (value) {
          ModalBottomSheetValue.Hidden -> BottomSheetStatus.CLOSED
          ModalBottomSheetValue.Expanded -> BottomSheetStatus.OPEN
          ModalBottomSheetValue.HalfExpanded -> BottomSheetStatus.HALF
        }
      }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun WrapInBottomSheet(
    onSwipe: (BottomSheetStatus) -> Unit = {},
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

      private val statusFlow by lazy { sheetState.toStatusFlow() }

      override fun show() {
        handleOpenSheet()
      }

      override fun hide() {
        handleCloseSheet()
      }

      override fun statusFlow(): Flow<BottomSheetStatus> {
        return statusFlow
      }
    }
  }

  val scrimColor = remember {
    Color.Black.copy(
        alpha = 0.4F,
    )
  }

  // Watch for a swipe causing a sheet change and update accordingly
  val handleSheetUpdated by rememberUpdatedState(onSwipe)
  LaunchedEffect(sheetState) { sheetState.toStatusFlow().collectLatest { handleSheetUpdated(it) } }

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
