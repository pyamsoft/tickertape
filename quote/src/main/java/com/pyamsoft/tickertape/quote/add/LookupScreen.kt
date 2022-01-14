package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.SearchResult

@Composable
@JvmOverloads
internal fun LookupScreen(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSymbolChanged: (String) -> Unit,
    onSearchResultSelected: (SearchResult) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
) {
  val symbol = state.symbol

  Column(
      modifier = modifier.padding(16.dp),
  ) {
    SymbolLookup(
        modifier = Modifier.fillMaxWidth(),
        symbol = symbol,
        onSymbolChanged = onSymbolChanged,
        onSubmit = onSubmit,
    )
    LookupResults(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        state = state,
        onSearchResultSelected = onSearchResultSelected,
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
  val symbol = state.symbol
  val isSubmitting = state.isSubmitting
  val isSubmitEnabled = remember(symbol, isSubmitting) { symbol.isNotBlank() && !isSubmitting }

  Row(
      modifier = modifier.padding(16.dp),
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
        modifier = Modifier.width(16.dp),
    )

    Button(
        enabled = isSubmitEnabled,
        modifier = Modifier.weight(1F),
        onClick = onSubmit,
    ) {
      Text(
          text = "Submit",
      )
    }
  }
}

@Composable
private fun LookupResults(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSearchResultSelected: (SearchResult) -> Unit,
) {
  val isLoading = state.isLookup
  Crossfade(
      modifier = modifier,
      targetState = isLoading,
  ) { loading ->
    if (loading) {
      LoadingResults(
          modifier = Modifier.fillMaxSize(),
      )
    } else {
      ResultList(
          modifier = Modifier.fillMaxSize(),
          state = state,
          onSearchResultSelected = onSearchResultSelected,
      )
    }
  }
}

@Composable
private fun ResultList(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onSearchResultSelected: (SearchResult) -> Unit,
) {
  val results = state.lookupResults
  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = 8.dp),
  ) {
    items(
        items = results,
        key = { it.symbol().symbol() },
    ) { item ->
      Divider(
          modifier = Modifier.fillMaxWidth(),
      )
      ResultItem(
          modifier = Modifier.fillMaxWidth(),
          item = item,
          onClick = onSearchResultSelected,
      )
    }
  }
}

@Composable
private fun ResultItem(
    modifier: Modifier = Modifier,
    item: SearchResult,
    onClick: (SearchResult) -> Unit,
) {
  val symbol = item.symbol()
  val company = item.name()

  Column(
      modifier = modifier.clickable { onClick(item) }.padding(8.dp),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Center,
  ) {
    Text(
        text = symbol.symbol(),
        style =
            MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.SemiBold,
            ),
    )
    Text(
        text = company.company(),
        style = MaterialTheme.typography.caption,
    )
  }
}

@Composable
private fun LoadingResults(
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier,
      contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
        modifier = Modifier.padding(16.dp),
    )
  }
}

@Composable
private fun SymbolLookup(
    modifier: Modifier = Modifier,
    symbol: String,
    onSymbolChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
  Box(
      modifier = modifier.padding(16.dp),
      contentAlignment = Alignment.Center,
  ) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = symbol,
        onValueChange = onSymbolChanged,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { onSubmit() },
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
    )
  }
}
