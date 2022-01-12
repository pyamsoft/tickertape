package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult

@Composable
@JvmOverloads
fun NewTickerScreen(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTypeSelected: (EquityType) -> Unit,
    onClose: () -> Unit,
    onSymbolChanged: (String) -> Unit,
    onSearchResultSelected: (SearchResult) -> Unit,
) {
  val equityType = state.equityType
  val hasEquitySelection = remember(equityType) { equityType != null }

  Surface(
      modifier = modifier,
      shape =
          MaterialTheme.shapes.medium.copy(
              bottomStart = ZeroCornerSize,
              bottomEnd = ZeroCornerSize,
          ),
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
          targetState = equityType,
      ) { et ->
        if (et != null) {
          LookupScreen(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              onSymbolChanged = onSymbolChanged,
              onSearchResultSelected = onSearchResultSelected,
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
      state = MutableNewTickerViewState().apply { this.equityType = equityType },
      onClose = {},
      onTypeSelected = {},
      onSymbolChanged = {},
      onSearchResultSelected = {},
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
