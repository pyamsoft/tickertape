package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.ui.rememberInBackground
import java.time.LocalDateTime

@Composable
internal fun OptionsSection(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTradeSideSelected: (TradeSide) -> Unit,
    onOptionTypeSlected: (StockOptions.Contract.Type) -> Unit,
    onExpirationDateSelected: (LocalDateTime) -> Unit,
    onStrikeSelected: (StockMoneyValue) -> Unit,
) {
  val show = remember(state.equityType) { state.equityType == EquityType.OPTION }
  AnimatedVisibility(
      modifier = modifier,
      visible = show,
  ) {
    Column {
      OptionsSide(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          state = state,
          onTradeSideSelected = onTradeSideSelected,
      )

      OptionsType(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          state = state,
          onOptionTypeSlected = onOptionTypeSlected,
      )

      OptionsStrikeExpiration(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          state = state,
          onExpirationDateSelected = onExpirationDateSelected,
          onStrikeSelected = onStrikeSelected,
      )
    }
  }
}

@Composable
private fun OptionsStrikeExpiration(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onExpirationDateSelected: (LocalDateTime) -> Unit,
    onStrikeSelected: (StockMoneyValue) -> Unit,
) {
  val option = state.resolvedOption ?: return

  val selectedExpirationDate = state.optionExpirationDate
  val allExpirationDates = option.expirationDates
  val dateFormatter = DATE_FORMATTER.get().requireNotNull()

  val selectedStrikePrice = state.optionStrikePrice
  val allStrikes = option.strikes

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    OptionsDropdown(
        modifier = Modifier.weight(1F),
        value = selectedExpirationDate,
        choices = allExpirationDates,
        onDisplayChoice = { it.format(dateFormatter) },
        onSelect = { onExpirationDateSelected(it) },
    )

    Spacer(
        modifier = Modifier.weight(1F),
    )

    OptionsDropdown(
        modifier = Modifier.weight(1F),
        value = selectedStrikePrice,
        choices = allStrikes,
        onDisplayChoice = { it.display },
        onSelect = { onStrikeSelected(it) },
    )
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

private data class OptionsDropdownItem<T>(
    val display: String,
    val value: T,
)

@Composable
private fun <T : Any> OptionsDropdown(
    modifier: Modifier = Modifier,
    value: T?,
    choices: List<T>,
    onDisplayChoice: (T) -> String,
    onSelect: (T) -> Unit,
) {
  val displayValue = remember(value) { if (value == null) "" else onDisplayChoice(value) }
  val (isDropdownOpen, setDropdownOpen) = remember { mutableStateOf(false) }

  val formatToDisplayChoice by rememberUpdatedState(onDisplayChoice)

  val displayChoices =
      rememberInBackground(choices) {
        choices.map { c ->
          OptionsDropdownItem(
              display = formatToDisplayChoice(c),
              value = c,
          )
        }
      }

  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    OutlinedTextField(
        modifier = Modifier.clickable { setDropdownOpen(true) },
        enabled = false,
        readOnly = true,
        singleLine = true,
        value = displayValue,
        onValueChange = {},
    )

    DropdownMenu(
        // Dropdown must have a max height or it goes over
        modifier =
            modifier.heightIn(
                max = 160.dp,
            ),
        expanded = isDropdownOpen,
        onDismissRequest = { setDropdownOpen(false) },
        properties =
            PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = true,
            ),
    ) {
      if (displayChoices != null) {
        for (choice in displayChoices) {
          DropdownMenuItem(
              onClick = { onSelect(choice.value) },
          ) {
            Text(
                modifier = Modifier.padding(MaterialTheme.keylines.baseline),
                text = choice.display,
            )
          }
        }
      }
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
        onStrikeSelected = {},
        onExpirationDateSelected = {},
    )
  }
}
