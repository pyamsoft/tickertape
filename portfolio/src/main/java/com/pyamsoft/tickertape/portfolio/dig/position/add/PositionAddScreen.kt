package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PositionAddScreen(
    modifier: Modifier = Modifier,
    state: PositionAddViewState,
    symbol: StockSymbol,
    onPriceChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onDateOfPurchaseClicked: (LocalDateTime?) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
  val isSubmitting = state.isSubmitting
  val isSubmittable = state.isSubmittable
  val pricePerShare = state.pricePerShare
  val numberOfShares = state.numberOfShares
  val dateOfPurchase = state.dateOfPurchase

  val isSubmitEnabled = remember(isSubmittable, isSubmitting) { isSubmittable && !isSubmitting }
  val isTextEntryEnabled = remember(isSubmitting) { !isSubmitting }
  val displayDate =
      remember(dateOfPurchase) {
        if (dateOfPurchase == null) "--/--/----"
        else dateOfPurchase.format(DateTimeFormatter.ISO_LOCAL_DATE)
      }

  Column(
      modifier = modifier,
  ) {
    PositionAddToolbar(
        modifier = Modifier.fillMaxWidth(),
        symbol = symbol,
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
            enabled = isTextEntryEnabled,
            readOnly = isTextEntryEnabled,
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
            enabled = isTextEntryEnabled,
            readOnly = isTextEntryEnabled,
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

        Box(
            modifier =
                Modifier.clickable { onDateOfPurchaseClicked(dateOfPurchase) }.padding(16.dp),
        ) {
          Text(
              text = displayDate,
              style = MaterialTheme.typography.body1,
          )
        }

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Spacer(
              modifier = Modifier.weight(1F),
          )
          Button(
              onClick = onSubmit,
              enabled = isSubmitEnabled,
          ) {
            Text(
                text = "Submit",
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewPositionAddScreen() {
  val symbol = "MSFT".asSymbol()
  PositionAddScreen(
      state = MutablePositionAddViewState(),
      symbol = symbol,
      onPriceChanged = {},
      onNumberChanged = {},
      onDateOfPurchaseClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
