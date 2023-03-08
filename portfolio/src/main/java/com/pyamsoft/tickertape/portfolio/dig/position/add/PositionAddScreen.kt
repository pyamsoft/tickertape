package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.ui.util.rememberStable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionPopup
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
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
  val isSubmitting by state.isSubmitting.collectAsState()
  val isSubmittable by state.isSubmittable.collectAsState()
  val pricePerShare by state.pricePerShare.collectAsState()
  val numberOfShares by state.numberOfShares.collectAsState()
  val dateOfPurchase by state.dateOfPurchase.collectAsState()
  val equityType by state.equityType.collectAsState()
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
      dateField = dateOfPurchase.rememberStable(),
      onDateClicked = onDateOfPurchaseClicked,
      onSubmit = onSubmit,
      onClose = onClose,
  )
}

@Preview
@Composable
private fun PreviewPositionAddScreen() {
  val symbol = "MSFT".asSymbol()
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
