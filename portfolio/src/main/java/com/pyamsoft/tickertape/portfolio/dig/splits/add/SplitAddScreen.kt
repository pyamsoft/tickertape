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

package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionPopup
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDate

@Composable
fun SplitAddScreen(
    modifier: Modifier = Modifier,
    state: SplitAddViewState,
    symbol: StockSymbol,
    onPreSplitCountChanged: (String) -> Unit,
    onPostSplitCountChanged: (String) -> Unit,
    onSplitDateClicked: (LocalDate?) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
  val isSubmitting by state.isSubmitting.collectAsStateWithLifecycle()
  val isSubmittable by state.isSubmittable.collectAsStateWithLifecycle()
  val preSplitShareCount by state.preSplitShareCount.collectAsStateWithLifecycle()
  val postSplitShareCount by state.postSplitShareCount.collectAsStateWithLifecycle()
  val splitDate by state.splitDate.collectAsStateWithLifecycle()

  val isSubmitEnabled =
      remember(
          isSubmittable,
          isSubmitting,
      ) {
        isSubmittable && !isSubmitting
      }

  BasePositionPopup(
      modifier = modifier,
      isReadOnly = isSubmitting,
      isSubmitEnabled = isSubmitEnabled,
      title = "Stock Split: ${symbol.raw}",
      topFieldLabel = "Pre-Split Share Count",
      topFieldValue = preSplitShareCount,
      onTopFieldChanged = onPreSplitCountChanged,
      bottomFieldLabel = "Post-Split Share Count",
      bottomFieldValue = postSplitShareCount,
      onBottomFieldChanged = onPostSplitCountChanged,
      dateLabel = "Date of Purchase",
      date = splitDate,
      onDateClicked = onSplitDateClicked,
      onSubmit = onSubmit,
      onClose = onClose,
  )
}

@Preview
@Composable
private fun PreviewSplitAddScreen() {
  val symbol = TestSymbol

  SplitAddScreen(
      state = MutableSplitAddViewState(),
      symbol = symbol,
      onPostSplitCountChanged = {},
      onPreSplitCountChanged = {},
      onSplitDateClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
