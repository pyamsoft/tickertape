package com.pyamsoft.tickertape.quote.dig.pricealert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.PriceAlertDigViewState
import com.pyamsoft.tickertape.quote.dig.base.BaseDigListScreen
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DigPriceAlerts(
    modifier: Modifier = Modifier,
    state: PriceAlertDigViewState,
    onRefresh: () -> Unit,
    onAddPriceAlert: () -> Unit,
    onUpdatePriceAlert: (PriceAlert) -> Unit,
    onDeletePriceAlert: (PriceAlert) -> Unit,
) {
  val isLoading = state.loadingState

  val isAddVisible = remember(isLoading) { !isLoading }

  BaseDigListScreen(
      modifier = modifier,
      label = "Add Position",
      isAddVisible = isAddVisible,
      items = state.priceAlerts,
      isLoading = isLoading,
      onRefresh = onRefresh,
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
  DigPriceAlerts(
      state =
          object :
              MutableDigViewState(
                  symbol = "MSFT".asSymbol(),
              ) {},
      onRefresh = {},
      onAddPriceAlert = {},
      onDeletePriceAlert = {},
      onUpdatePriceAlert = {},
  )
}
