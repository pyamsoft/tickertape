package com.pyamsoft.tickertape.quote.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope

@Composable
@JvmOverloads
internal fun LookupScreen(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSymbolChanged: (String) -> Unit,
    onSearchResultSelected: (SearchResult) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onTradeSideSelected: (TradeSide) -> Unit,
    onOptionTypeSelected: (StockOptions.Contract.Type) -> Unit,
    onResultsDismissed: () -> Unit,
    onExpirationDateSelected: (LocalDate) -> Unit,
    onStrikeSelected: (StockMoneyValue) -> Unit,
    onAfterSymbolChanged: CoroutineScope.(String) -> Unit,
) {
  Column(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    SymbolLookup(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        state = state,
        onSubmit = onSubmit,
        onSymbolChanged = onSymbolChanged,
        onAfterSymbolChanged = onAfterSymbolChanged,
    )
    LookupResults(
        state = state,
        onSearchResultSelected = onSearchResultSelected,
        onResultsDismissed = onResultsDismissed,
    )
    OptionsSection(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        state = state,
        onTradeSideSelected = onTradeSideSelected,
        onOptionTypeSlected = onOptionTypeSelected,
        onStrikeSelected = onStrikeSelected,
        onExpirationDateSelected = onExpirationDateSelected,
    )
    SubmissionSection(
        modifier = Modifier.fillMaxWidth(),
        state = state,
        onSubmit = onSubmit,
        onClear = onClear,
    )
  }
}

@Composable
private fun SubmissionSection(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
) {
  val canSubmit = state.canSubmit()
  val isSubmitting = state.isSubmitting

  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.Center,
  ) {
    Divider(
        modifier = Modifier.fillMaxWidth(),
    )

    Row(
        modifier = Modifier.padding(top = MaterialTheme.keylines.content),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedButton(
          enabled = !isSubmitting,
          modifier = Modifier.weight(1F),
          onClick = onClear,
      ) {
        Text(
            text = "Clear",
        )
      }

      Spacer(
          modifier = Modifier.width(MaterialTheme.keylines.content),
      )

      Button(
          enabled = canSubmit,
          modifier = Modifier.weight(1F),
          onClick = onSubmit,
      ) {
        Text(
            text = "Submit",
        )
      }
    }
  }
}

@Composable
private fun LookupResults(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSearchResultSelected: (SearchResult) -> Unit,
    onResultsDismissed: () -> Unit,
) {
  val results = state.lookupResults
  val isOpen = remember(results) { results.isNotEmpty() }

  DropdownMenu(
      // Dropdown must have a max height or it goes over
      modifier =
          modifier.heightIn(
              max = 160.dp,
          ),
      expanded = isOpen,
      onDismissRequest = onResultsDismissed,
      properties =
          PopupProperties(
              focusable = false,
              dismissOnBackPress = false,
              dismissOnClickOutside = true,
          ),
  ) {
    for (result in results) {
      DropdownMenuItem(
          onClick = { onSearchResultSelected(result) },
      ) {
        ResultItem(
            modifier = Modifier.fillMaxWidth(),
            result = result,
        )
      }
    }
  }
}

@Composable
private fun ResultItem(
    modifier: Modifier = Modifier,
    result: SearchResult,
) {
  val symbol = result.symbol
  val company = result.name

  Column(
      modifier = modifier.padding(MaterialTheme.keylines.baseline),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Center,
  ) {
    Text(
        text = symbol.raw,
        style =
            MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.W500,
            ),
    )
    if (company.isValid) {
      Text(
          text = company.company,
          style = MaterialTheme.typography.caption,
      )
    }
  }
}

@Composable
private fun SymbolLookup(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSymbolChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onAfterSymbolChanged: CoroutineScope.(String) -> Unit,
) {
  val symbol = state.symbol
  val isSubmitOnEnter = remember(state.equityType) { state.equityType != EquityType.OPTION }

  val focusManager = LocalFocusManager.current

  LaunchedEffect(symbol) { onAfterSymbolChanged(symbol) }

  val focusRequester = remember { FocusRequester() }

  // Request focus to top field on launch
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.Center,
  ) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        value = symbol,
        onValueChange = onSymbolChanged,
        visualTransformation = { text ->
          TransformedText(
              text =
                  AnnotatedString(
                      text = text.text.uppercase(),
                      spanStyles = text.spanStyles,
                      paragraphStyles = text.paragraphStyles,
                  ),
              offsetMapping = OffsetMapping.Identity,
          )
        },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = {
                  if (isSubmitOnEnter) {
                    onSubmit()
                  } else {
                    focusManager.moveFocus(FocusDirection.Down)
                  }
                },
            ),
        singleLine = true,
        label = {
          Text(
              text = "Enter a ticker symbol...",
          )
        },
        leadingIcon = {
          Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search",
          )
        },
    )
  }
}

@Preview
@Composable
private fun PreviewLookupScreen() {
  Surface {
    LookupScreen(
        state = MutableNewTickerViewState(),
        onSymbolChanged = {},
        onSearchResultSelected = {},
        onSubmit = {},
        onClear = {},
        onTradeSideSelected = {},
        onResultsDismissed = {},
        onOptionTypeSelected = {},
        onExpirationDateSelected = {},
        onStrikeSelected = {},
        onAfterSymbolChanged = {},
    )
  }
}
