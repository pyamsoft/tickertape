package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.icon.Paid
import com.pyamsoft.tickertape.ui.icon.Tag
import com.pyamsoft.tickertape.ui.icon.Today
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PositionAddScreen(
    modifier: Modifier = Modifier,
    state: PositionAddViewState,
    symbol: StockSymbol,
    onPriceChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onDateOfPurchaseClicked: (LocalDate?) -> Unit,
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

  val focusManager = LocalFocusManager.current

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
        NumberOfShares(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            isEnabled = isTextEntryEnabled,
            numberOfShares = numberOfShares,
            onNumberChanged = onNumberChanged,
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
        )

        PricePerShare(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            isEnabled = isTextEntryEnabled,
            pricePerShare = pricePerShare,
            onPriceChanged = onPriceChanged,
            onNext = { onDateOfPurchaseClicked(dateOfPurchase) },
        )

        DateOfPurchase(
            modifier = Modifier.padding(8.dp),
            dateOfPurchase = dateOfPurchase,
            onDateOfPurchaseClicked = onDateOfPurchaseClicked,
        )

        SubmitSection(
            modifier = Modifier.padding(bottom = 16.dp),
            isEnabled = isSubmitEnabled,
            onSubmit = onSubmit,
        )
      }
    }
  }
}

@Composable
private fun SubmitSection(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onSubmit: () -> Unit,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(
        modifier = Modifier.weight(1F),
    )
    Button(
        onClick = onSubmit,
        enabled = isEnabled,
    ) {
      Text(
          text = "Submit",
      )
    }
  }
}

@Composable
private fun DateOfPurchase(
    modifier: Modifier = Modifier,
    dateOfPurchase: LocalDate?,
    onDateOfPurchaseClicked: (LocalDate?) -> Unit
) {
  val displayDate =
      remember(dateOfPurchase) {
        if (dateOfPurchase == null) "--/--/----"
        else dateOfPurchase.format(DateTimeFormatter.ISO_LOCAL_DATE)
      }

  Row(
      modifier = modifier.clickable { onDateOfPurchaseClicked(dateOfPurchase) },
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        modifier = Modifier.padding(end = 20.dp).alpha(TextFieldDefaults.IconOpacity),
        imageVector = Icons.Filled.Today,
        contentDescription = "Number of shares",
    )
    Text(
        text = displayDate,
        style = MaterialTheme.typography.body1,
    )
  }
}

@Composable
private fun NumberOfShares(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    numberOfShares: String,
    onNumberChanged: (String) -> Unit,
    onNext: () -> Unit,
) {
  OutlinedTextField(
      modifier = modifier,
      enabled = isEnabled,
      readOnly = !isEnabled,
      value = numberOfShares,
      onValueChange = onNumberChanged,
      keyboardOptions =
          KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next,
          ),
      keyboardActions =
          KeyboardActions(
              onNext = { onNext() },
          ),
      label = {
        Text(
            text = "Number of shares",
        )
      },
      leadingIcon = {
        Icon(
            modifier = Modifier.padding(end = 4.dp),
            imageVector = Icons.Filled.Tag,
            contentDescription = "Number of shares",
        )
      },
  )
}

@Composable
private fun PricePerShare(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    pricePerShare: String,
    onPriceChanged: (String) -> Unit,
    onNext: () -> Unit,
) {
  OutlinedTextField(
      modifier = modifier,
      enabled = isEnabled,
      readOnly = !isEnabled,
      value = pricePerShare,
      onValueChange = onPriceChanged,
      keyboardOptions =
          KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next,
          ),
      keyboardActions =
          KeyboardActions(
              onNext = { onNext() },
          ),
      label = {
        Text(
            text = "Price per share",
        )
      },
      leadingIcon = {
        Icon(
            modifier = Modifier.padding(end = 4.dp),
            imageVector = Icons.Filled.Paid,
            contentDescription = "Price per share",
        )
      },
  )
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
