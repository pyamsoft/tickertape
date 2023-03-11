package com.pyamsoft.tickertape.quote.dig.pricealert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.ui.util.collectAsStateList
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
    onRefresh: () -> Unit,
    onAddPriceAlert: () -> Unit,
    onUpdatePriceAlert: (PriceAlert) -> Unit,
    onDeletePriceAlert: (PriceAlert) -> Unit,
) {
  val loadingState by state.loadingState.collectAsState()
  val priceAlerts = state.priceAlerts.collectAsStateList()

  val isRefreshing =
      remember(loadingState) { loadingState == BaseDigViewState.LoadingState.LOADING }

  val isAddVisible = remember(loadingState) { loadingState == BaseDigViewState.LoadingState.DONE }

  BaseDigListScreen(
      modifier = modifier,
      label = "Add Position",
      isAddVisible = isAddVisible,
      items = priceAlerts,
      isLoading = isRefreshing,
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
  val symbol = TestSymbol
  val clock = TestClock

  DigPriceAlerts(
      state =
          object :
              MutableDigViewState(
                  symbol = symbol,
                  clock = clock,
              ) {},
      onRefresh = {},
      onAddPriceAlert = {},
      onDeletePriceAlert = {},
      onUpdatePriceAlert = {},
  )
}
