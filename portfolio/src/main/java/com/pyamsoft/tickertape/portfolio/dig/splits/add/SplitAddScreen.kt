package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.ui.util.rememberStable
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
  val isSubmitting by state.isSubmitting.collectAsState()
  val isSubmittable by state.isSubmittable.collectAsState()
  val preSplitShareCount by state.preSplitShareCount.collectAsState()
  val postSplitShareCount by state.postSplitShareCount.collectAsState()
  val splitDate by state.splitDate.collectAsState()

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
      dateField = splitDate.rememberStable(),
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
      state = MutableSplitAddViewState(),
      symbol = symbol,
      onPostSplitCountChanged = {},
      onPreSplitCountChanged = {},
      onSplitDateClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
