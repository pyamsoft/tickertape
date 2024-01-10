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

package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionPopup
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDate

@Composable
fun PositionAddScreen(
    modifier: Modifier = Modifier,
    state: PositionAddViewState,
    symbol: StockSymbol,
    onPriceChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onDateOfPurchaseClicked: (LocalDate?) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
  val isSubmitting by state.isSubmitting.collectAsStateWithLifecycle()
  val isSubmittable by state.isSubmittable.collectAsStateWithLifecycle()
  val pricePerShare by state.pricePerShare.collectAsStateWithLifecycle()
  val numberOfShares by state.numberOfShares.collectAsStateWithLifecycle()
  val dateOfPurchase by state.dateOfPurchase.collectAsStateWithLifecycle()
  val equityType by state.equityType.collectAsStateWithLifecycle()
  val isOption = remember(equityType) { equityType == EquityType.OPTION }

  val isSubmitEnabled = remember(isSubmittable, isSubmitting) { isSubmittable && !isSubmitting }
  val isReadOnly = remember(isSubmitting) { isSubmitting }

  val what = remember(isOption) { if (isOption) "Contract" else "Share" }

  BasePositionPopup(
      modifier = modifier,
      isReadOnly = isReadOnly,
      isSubmitEnabled = isSubmitEnabled,
      title = "Position: ${symbol.raw}",
      topFieldLabel = "Number of ${what}s",
      topFieldValue = numberOfShares,
      onTopFieldChanged = onNumberChanged,
      bottomFieldLabel = "Price per $what",
      bottomFieldValue = pricePerShare,
      onBottomFieldChanged = onPriceChanged,
      dateLabel = "Date of Purchase",
      date = dateOfPurchase,
      onDateClicked = onDateOfPurchaseClicked,
      onSubmit = onSubmit,
      onClose = onClose,
  )
}

@Preview
@Composable
private fun PreviewPositionAddScreen() {
  val symbol = TestSymbol

  PositionAddScreen(
      state =
          MutablePositionAddViewState(
              params =
                  PositionParams(
                      symbol = symbol,
                      holdingId = DbHolding.Id.EMPTY,
                      holdingType = EquityType.STOCK,
                      existingPositionId = DbPosition.Id.EMPTY,
                  ),
          ),
      symbol = symbol,
      onPriceChanged = {},
      onNumberChanged = {},
      onDateOfPurchaseClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
