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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.icon.Paid
import com.pyamsoft.tickertape.ui.icon.Tag
import com.pyamsoft.tickertape.ui.icon.Today
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import timber.log.Timber

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
  val equityType = state.equityType
  val isOption = remember(equityType) { equityType == EquityType.OPTION }

  val isSubmitEnabled = remember(isSubmittable, isSubmitting) { isSubmittable && !isSubmitting }
  val isReadOnly = remember(isSubmitting) { isSubmitting }

  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }

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
            modifier =
                Modifier.fillMaxWidth().padding(bottom = 8.dp).focusRequester(focusRequester),
            isOption = isOption,
            readOnly = isReadOnly,
            numberOfShares = numberOfShares,
            onNumberChanged = onNumberChanged,
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
        )

        PricePerShare(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            isOption = isOption,
            readOnly = isReadOnly,
            pricePerShare = pricePerShare,
            onPriceChanged = onPriceChanged,
            onNext = { onDateOfPurchaseClicked(dateOfPurchase) },
        )

        DateOfPurchase(
            modifier = Modifier.padding(8.dp),
            readOnly = isReadOnly,
            dateOfPurchase = dateOfPurchase,
            onDateOfPurchaseClicked = onDateOfPurchaseClicked,
        )

        SubmitSection(
            modifier = Modifier.padding(bottom = 16.dp),
            isEnabled = isSubmitEnabled,
            onSubmit = {
              onSubmit()

              Timber.d("Re-request focus on top field")
              focusRequester.requestFocus()
            },
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
    readOnly: Boolean,
    dateOfPurchase: LocalDate?,
    onDateOfPurchaseClicked: (LocalDate?) -> Unit
) {
  val displayDate =
      remember(dateOfPurchase) {
        if (dateOfPurchase == null) "--/--/----"
        else dateOfPurchase.format(DateTimeFormatter.ISO_LOCAL_DATE)
      }

  Row(
      modifier =
          modifier.clickable(
              enabled = !readOnly,
          ) { onDateOfPurchaseClicked(dateOfPurchase) },
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        modifier = Modifier.padding(end = 20.dp).alpha(TextFieldDefaults.IconOpacity),
        imageVector = Icons.Filled.Today,
        contentDescription = "Date of Purchase",
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
    isOption: Boolean,
    readOnly: Boolean,
    numberOfShares: String,
    onNumberChanged: (String) -> Unit,
    onNext: () -> Unit,
) {
  val what = remember(isOption) { if (isOption) "Contracts" else "Shares" }
  OutlinedTextField(
      modifier = modifier,
      readOnly = readOnly,
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
            text = "Number of $what",
        )
      },
      leadingIcon = {
        Icon(
            modifier = Modifier.padding(end = 4.dp),
            imageVector = Icons.Filled.Tag,
            contentDescription = "Number of $what",
        )
      },
  )
}

@Composable
private fun PricePerShare(
    modifier: Modifier = Modifier,
    isOption: Boolean,
    readOnly: Boolean,
    pricePerShare: String,
    onPriceChanged: (String) -> Unit,
    onNext: () -> Unit,
) {
  val what = remember(isOption) { if (isOption) "Contract" else "Share" }
  OutlinedTextField(
      modifier = modifier,
      readOnly = readOnly,
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
            text = "Price per $what",
        )
      },
      leadingIcon = {
        Icon(
            modifier = Modifier.padding(end = 4.dp),
            imageVector = Icons.Filled.Paid,
            contentDescription = "Price per $what",
        )
      },
  )
}

@Preview
@Composable
private fun PreviewPositionAddScreen() {
  val symbol = "MSFT".asSymbol()
  PositionAddScreen(
      state = MutablePositionAddViewState(EquityType.STOCK),
      symbol = symbol,
      onPriceChanged = {},
      onNumberChanged = {},
      onDateOfPurchaseClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
