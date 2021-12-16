package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PositionScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
) {
}

@Preview
@Composable
private fun PreviewPositionScreen() {
  PositionScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = "MSFT".asSymbol(),
          ),
  )
}
