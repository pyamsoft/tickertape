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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope

@Composable
@JvmOverloads
fun NewTickerScreen(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTypeSelected: (EquityType) -> Unit,
    onClose: () -> Unit,
    onSymbolChanged: (String) -> Unit,
    onAfterSymbolChanged: CoroutineScope.(String) -> Unit,
    onSearchResultSelected: (SearchResult) -> Unit,
    onResultsDismissed: () -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onTradeSideSelected: (TradeSide) -> Unit,
    onOptionTypeSlected: (StockOptions.Contract.Type) -> Unit,
    onExpirationDateSelected: (LocalDate) -> Unit,
    onStrikeSelected: (StockMoneyValue) -> Unit,
) {
  val equityType by state.equityType.collectAsStateWithLifecycle()

  val hasEquitySelection = remember(equityType) { equityType != null }

  Surface(
      modifier = modifier,
      shape =
          MaterialTheme.shapes.medium.copy(
              bottomStart = ZeroCornerSize,
              bottomEnd = ZeroCornerSize,
          ),
      elevation = DialogDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      TickerAddTopBar(
          modifier = Modifier.fillMaxWidth(),
          hasEquitySelection = hasEquitySelection,
          onClose = onClose,
      )

      Crossfade(
          modifier = Modifier.fillMaxWidth(),
          label = "LookupOrEquityTypePicker",
          targetState = equityType,
      ) { et ->
        if (et != null) {
          LookupScreen(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              onSymbolChanged = onSymbolChanged,
              onSearchResultSelected = onSearchResultSelected,
              onSubmit = onSubmit,
              onClear = onClear,
              onTradeSideSelected = onTradeSideSelected,
              onResultsDismissed = onResultsDismissed,
              onOptionTypeSelected = onOptionTypeSlected,
              onExpirationDateSelected = onExpirationDateSelected,
              onStrikeSelected = onStrikeSelected,
              onAfterSymbolChanged = onAfterSymbolChanged,
          )
        } else {
          EquitySelectionScreen(
              modifier = Modifier.fillMaxWidth(),
              onTypeSelected = onTypeSelected,
          )
        }
      }
    }
  }
}

@Composable
private fun PreviewNewTickerScreen(equityType: EquityType?) {
  NewTickerScreen(
      state = MutableNewTickerViewState().apply { this.equityType.value = equityType },
      onClose = {},
      onTypeSelected = {},
      onSymbolChanged = {},
      onSearchResultSelected = {},
      onSubmit = {},
      onClear = {},
      onTradeSideSelected = {},
      onResultsDismissed = {},
      onOptionTypeSlected = {},
      onExpirationDateSelected = {},
      onStrikeSelected = {},
      onAfterSymbolChanged = {},
  )
}

@Preview
@Composable
private fun PreviewNewTickerScreenNoSelection() {
  PreviewNewTickerScreen(
      equityType = null,
  )
}

@Preview
@Composable
private fun PreviewNewTickerScreenWithSelection() {
  PreviewNewTickerScreen(
      equityType = EquityType.STOCK,
  )
}
