/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.dig.pricealert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.PriceAlertDigViewState
import com.pyamsoft.tickertape.quote.dig.base.BaseDigListScreen
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DigPriceAlerts(
    modifier: Modifier = Modifier,
    state: PriceAlertDigViewState,
    onAddPriceAlert: () -> Unit,
    onUpdatePriceAlert: (PriceAlert) -> Unit,
    onDeletePriceAlert: (PriceAlert) -> Unit,
) {
  val loadingState by state.loadingState.collectAsStateWithLifecycle()
  val priceAlerts = state.priceAlerts.collectAsStateListWithLifecycle()

  val isAddVisible = remember(loadingState) { loadingState == BaseDigViewState.LoadingState.DONE }

  BaseDigListScreen(
      modifier = modifier,
      label = "Add Position",
      isAddVisible = isAddVisible,
      items = priceAlerts,
      onAddClicked = onAddPriceAlert,
      itemKey = { it.id.raw },
  ) { priceAlert ->
    PriceAlertItem(
        modifier =
            Modifier.fillMaxWidth()
                .combinedClickable(
                    onClick = { onUpdatePriceAlert(priceAlert) },
                    onLongClick = { onDeletePriceAlert(priceAlert) },
                ),
        priceAlert = priceAlert,
    )
  }
}

@Preview
@Composable
private fun PreviewDigPriceAlerts() {
  val symbol = TestSymbol
  val clock = TestClock

  DigPriceAlerts(
      state =
          object :
              MutableDigViewState(
                  symbol = symbol,
                  clock = clock,
              ) {},
      onAddPriceAlert = {},
      onDeletePriceAlert = {},
      onUpdatePriceAlert = {},
  )
}
