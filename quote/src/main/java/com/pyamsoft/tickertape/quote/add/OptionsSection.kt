package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide

@Composable
@OptIn(ExperimentalAnimationApi::class)
internal fun OptionsSection(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTradeSideSelected: (TradeSide) -> Unit,
) {
  val show = remember(state.equityType) { state.equityType == EquityType.OPTION }
  AnimatedVisibility(
      modifier = modifier,
      visible = show,
  ) {
    Column {
      OptionsSide(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          state = state,
          onTradeSideSelected = onTradeSideSelected,
      )
    }
  }
}

@Composable
private fun OptionsSide(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTradeSideSelected: (TradeSide) -> Unit,
) {
  val side = state.tradeSide
  val onBuyClicked = remember { { onTradeSideSelected(TradeSide.BUY) } }
  val onSellClicked = remember { { onTradeSideSelected(TradeSide.SELL) } }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    val buttonModifier = Modifier.weight(1F)
    val buyButtonContent =
        @Composable
        {
          Text(
              text = "BUY",
          )
        }

    val sellButtonContent =
        @Composable
        {
          Text(
              text = "SELL",
          )
        }

    if (side == TradeSide.BUY) {
      Button(
          modifier = buttonModifier,
          onClick = onBuyClicked,
      ) { buyButtonContent() }
      OutlinedButton(
          modifier = buttonModifier,
          onClick = onSellClicked,
      ) { sellButtonContent() }
    } else {
      OutlinedButton(
          modifier = buttonModifier,
          onClick = onBuyClicked,
      ) { buyButtonContent() }
      Button(
          modifier = buttonModifier,
          onClick = onSellClicked,
      ) { sellButtonContent() }
    }
  }
}

@Preview
@Composable
private fun PreviewOptionsSection() {
  Surface {
    OptionsSection(
        state = MutableNewTickerViewState(),
        onTradeSideSelected = {},
    )
  }
}
