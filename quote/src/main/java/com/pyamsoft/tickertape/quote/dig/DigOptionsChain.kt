package com.pyamsoft.tickertape.quote.dig

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme

@Composable
fun DigOptionsChain(
    modifier: Modifier = Modifier,
    state: DigViewState,
) {}

@Preview
@Composable
private fun PreviewDigOptionsChain() {
  PreviewTickerTapeTheme {
    Surface {
      DigOptionsChain(
          modifier = Modifier.padding(16.dp),
          state =
              object :
                  MutableDigViewState(
                      symbol = "MSFT".asSymbol(),
                  ) {},
      )
    }
  }
}
