package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    LookupResults(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        state = state,
        onSearchResultSelected = onSearchResultSelected,
    )
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
  Surface {
    LookupScreen(
        state = MutableNewTickerViewState(),
        onSymbolChanged = {},
        onSearchResultSelected = {},
    )
  }
}
