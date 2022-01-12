package com.pyamsoft.tickertape.quote.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@JvmOverloads
internal fun LookupScreen(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSymbolChanged: (String) -> Unit,
) {
  val symbol = state.symbol

  Column(
      modifier = modifier.padding(16.dp),
  ) {
    SymbolLookup(
        modifier = Modifier.fillMaxWidth(),
        symbol = symbol,
        onSymbolChanged = onSymbolChanged,
    )
  }
}

@Composable
private fun SymbolLookup(
    modifier: Modifier = Modifier,
    symbol: String,
    onSymbolChanged: (String) -> Unit,
) {
  Box(
      modifier = modifier.padding(16.dp),
      contentAlignment = Alignment.Center,
  ) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = symbol,
        onValueChange = onSymbolChanged,
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
  LookupScreen(
      state = MutableNewTickerViewState(),
      onSymbolChanged = {},
  )
}
