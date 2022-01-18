package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.TradeSide

@Composable
@OptIn(ExperimentalAnimationApi::class)
internal fun OptionsSection(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTradeSideSelected: (TradeSide) -> Unit,
    onOptionTypeSlected: (StockOptions.Contract.Type) -> Unit,
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

      OptionsType(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          state = state,
          onOptionTypeSlected = onOptionTypeSlected,
      )

      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {}
    }
  }
}

@Composable
private fun OptionsType(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onOptionTypeSlected: (StockOptions.Contract.Type) -> Unit,
) {
  val type = state.optionType
  ButtonBar(
      modifier = modifier,
      isReverse = type == StockOptions.Contract.Type.PUT,
      onFirstButtonClicked = { onOptionTypeSlected(StockOptions.Contract.Type.CALL) },
      onSecondButtonClicked = { onOptionTypeSlected(StockOptions.Contract.Type.PUT) },
      firstButtonContent = {
        Text(
            text = "CALLS",
        )
      },
      secondButtonContent = {
        Text(
            text = "PUTS",
        )
      },
  )
}

@Composable
private fun OptionsSide(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTradeSideSelected: (TradeSide) -> Unit,
) {
  val side = state.tradeSide
  ButtonBar(
      modifier = modifier,
      isReverse = side == TradeSide.SELL,
      onFirstButtonClicked = { onTradeSideSelected(TradeSide.BUY) },
      onSecondButtonClicked = { onTradeSideSelected(TradeSide.SELL) },
      firstButtonContent = {
        Text(
            text = "BUY",
        )
      },
      secondButtonContent = {
        Text(
            text = "SELL",
        )
      },
  )
}

@Composable
private fun ButtonBar(
    modifier: Modifier = Modifier,
    isReverse: Boolean,
    onFirstButtonClicked: () -> Unit,
    onSecondButtonClicked: () -> Unit,
    firstButtonContent: @Composable RowScope.() -> Unit,
    secondButtonContent: @Composable RowScope.() -> Unit,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    val buttonModifier = Modifier.weight(1F)
    val firstButtonShape =
        MaterialTheme.shapes.small.copy(
            topEnd = ZeroCornerSize,
            bottomEnd = ZeroCornerSize,
        )

    val secondButtonShape =
        MaterialTheme.shapes.small.copy(
            topStart = ZeroCornerSize,
            bottomStart = ZeroCornerSize,
        )

    if (isReverse) {
      OutlinedButton(
          modifier = buttonModifier,
          onClick = onFirstButtonClicked,
          shape = firstButtonShape,
          content = firstButtonContent,
      )
      Button(
          modifier = buttonModifier,
          onClick = onSecondButtonClicked,
          shape = secondButtonShape,
          content = secondButtonContent,
      )
    } else {
      Button(
          modifier = buttonModifier,
          onClick = onFirstButtonClicked,
          shape = firstButtonShape,
          content = firstButtonContent,
      )
      OutlinedButton(
          modifier = buttonModifier,
          onClick = onSecondButtonClicked,
          shape = secondButtonShape,
          content = secondButtonContent,
      )
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
        onOptionTypeSlected = {},
    )
  }
}
