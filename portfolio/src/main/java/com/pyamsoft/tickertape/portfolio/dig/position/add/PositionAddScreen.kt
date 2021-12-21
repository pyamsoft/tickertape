package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Removes anything that isn't a number or the decimal point
private val DECIMAL_NUMBER_REGEX = Regex("[^0-9.]")

@CheckResult
private fun textToDecimalNumber(text: String): Double {
  return text.trim().replace(DECIMAL_NUMBER_REGEX, "").toDoubleOrNull() ?: 0.0
}

@Composable
fun PositionAddScreen(
    modifier: Modifier = Modifier,
    state: PositionAddViewState,
    onPriceChanged: (Double) -> Unit,
    onNumberChanged: (Double) -> Unit,
    onClose: () -> Unit,
) {
  val pricePerShareValue = state.pricePerShare
  val pricePerShare = remember(pricePerShareValue) { pricePerShareValue.toString() }

  val numberOfSharesValue = state.numberOfShares
  val numberOfShares = remember(numberOfSharesValue) { numberOfSharesValue.toString() }
  Column(
      modifier = modifier,
  ) {
    PositionAddToolbar(
        modifier = Modifier.fillMaxWidth(),
        onClose = onClose,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
      ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            value = pricePerShare,
            onValueChange = { onPriceChanged(textToDecimalNumber(it)) },
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            value = numberOfShares,
            onValueChange = { onNumberChanged(textToDecimalNumber(it)) },
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewPositionAddScreen() {
  PositionAddScreen(
      state = MutablePositionAddViewState(),
      onPriceChanged = {},
      onNumberChanged = {},
      onClose = {},
  )
}
