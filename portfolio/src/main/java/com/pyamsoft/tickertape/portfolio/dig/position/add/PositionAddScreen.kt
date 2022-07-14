package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionPopup
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
  val isSubmitting = state.isSubmitting
  val isSubmittable = state.isSubmittable
  val pricePerShare = state.pricePerShare
  val numberOfShares = state.numberOfShares
  val dateOfPurchase = state.dateOfPurchase
  val equityType = state.equityType
  val isOption = remember(equityType) { equityType == EquityType.OPTION }

  val isSubmitEnabled = remember(isSubmittable, isSubmitting) { isSubmittable && !isSubmitting }
  val isReadOnly = remember(isSubmitting) { isSubmitting }

  val what = remember(isOption) { if (isOption) "Contract" else "Share" }

  BasePositionPopup(
      modifier = modifier,
      isReadOnly = isReadOnly,
      isSubmitEnabled = isSubmitEnabled,
      title = "Position: ${symbol.symbol()}",
      topFieldLabel = "Number of ${what}s",
      topFieldValue = numberOfShares,
      onTopFieldChanged = onNumberChanged,
      bottomFieldLabel = "Price per $what",
      bottomFieldValue = pricePerShare,
      onBottomFieldChanged = onPriceChanged,
      dateLabel = "Date of Purchase",
      dateField = dateOfPurchase,
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
              equityType = EquityType.STOCK,
              existingPositionId = DbPosition.Id.EMPTY,
          ),
      symbol = symbol,
      onPriceChanged = {},
      onNumberChanged = {},
      onDateOfPurchaseClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
