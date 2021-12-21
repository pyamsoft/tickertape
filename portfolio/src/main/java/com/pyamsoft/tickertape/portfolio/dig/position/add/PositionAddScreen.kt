package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PositionAddScreen(
    modifier: Modifier = Modifier,
    state: PositionAddViewState,
    onPriceChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onClose: () -> Unit,
) {
  val pricePerShare = state.pricePerShare
  val numberOfShares = state.numberOfShares

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
            value = pricePerShare,
            onValueChange = onPriceChanged,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            label = {
              Text(
                  text = "Price per share",
              )
            },
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            value = numberOfShares,
            onValueChange = onNumberChanged,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            label = {
              Text(
                  text = "Number of shares",
              )
            },
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
