package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionPopup
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
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
  val isSubmitting = state.isSubmitting
  val isSubmittable = state.isSubmittable
  val preSplitShareCount = state.preSplitShareCount
  val postSplitShareCount = state.postSplitShareCount
  val splitDate = state.splitDate

  val isSubmitEnabled = remember(isSubmittable, isSubmitting) { isSubmittable && !isSubmitting }
  val isReadOnly = remember(isSubmitting) { isSubmitting }

  BasePositionPopup(
      modifier = modifier,
      isReadOnly = isReadOnly,
      isSubmitEnabled = isSubmitEnabled,
      title = "Stock Split: ${symbol.raw}",
      topFieldLabel = "Pre-Split Share Count",
      topFieldValue = preSplitShareCount,
      onTopFieldChanged = onPreSplitCountChanged,
      bottomFieldLabel = "Post-Split Share Count",
      bottomFieldValue = postSplitShareCount,
      onBottomFieldChanged = onPostSplitCountChanged,
      dateLabel = "Date of Purchase",
      dateField = splitDate,
      onDateClicked = onSplitDateClicked,
      onSubmit = onSubmit,
      onClose = onClose,
  )
}

@Preview
@Composable
private fun PreviewSplitAddScreen() {
  val symbol = "MSFT".asSymbol()
  SplitAddScreen(
      state =
          MutableSplitAddViewState(
              existingSplitId = DbSplit.Id.EMPTY,
          ),
      symbol = symbol,
      onPostSplitCountChanged = {},
      onPreSplitCountChanged = {},
      onSplitDateClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
