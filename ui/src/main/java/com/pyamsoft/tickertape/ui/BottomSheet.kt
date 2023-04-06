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

package com.pyamsoft.tickertape.ui

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface BottomSheetController {

  fun show()

  fun hide()

  val status: BottomSheetStatus

  @CheckResult fun statusFlow(): Flow<BottomSheetStatus>
}

enum class BottomSheetStatus {
  OPEN,
  HALF,
  CLOSED
}

@CheckResult
@OptIn(ExperimentalMaterialApi::class)
private fun ModalBottomSheetValue.toStatus(): BottomSheetStatus =
    when (this) {
      ModalBottomSheetValue.Hidden -> BottomSheetStatus.CLOSED
      ModalBottomSheetValue.Expanded -> BottomSheetStatus.OPEN
      ModalBottomSheetValue.HalfExpanded -> BottomSheetStatus.HALF
    }

@CheckResult
@OptIn(ExperimentalMaterialApi::class)
private fun ModalBottomSheetState.toStatusFlow(): Flow<BottomSheetStatus> {
  return snapshotFlow { this.targetValue }.map { it.toStatus() }
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

      override val status
        get() = sheetState.currentValue.toStatus()

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
  LaunchedEffect(sheetState) { sheetState.toStatusFlow().collect { handleSheetUpdated(it) } }

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
