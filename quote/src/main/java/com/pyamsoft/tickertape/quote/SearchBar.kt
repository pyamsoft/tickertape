package com.pyamsoft.tickertape.quote

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.EquityType
import kotlinx.coroutines.CoroutineScope

@Composable
@JvmOverloads
fun SearchBar(
    modifier: Modifier = Modifier,
    search: String,
    currentTab: EquityType,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
) {
  val allTypes = remember { EquityType.values() }
  val contentColor = LocalContentColor.current
  val selectedTabIndex = currentTab.ordinal

  LaunchedEffect(search, currentTab) {
    // Fire the processing effect after the search or tab changes
    this.onRegenerateList()
  }

  Column(
      modifier = modifier.fillMaxWidth(),
  ) {
    SearchInput(
        modifier = Modifier.fillMaxWidth(),
        search = search,
        onSearchChanged = onSearchChanged,
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = Color.Transparent,
        contentColor = contentColor,
        indicator = { tabPositions ->
          TabRowDefaults.Indicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
              color = MaterialTheme.colors.secondary,
          )
        },
    ) {
      allTypes.forEach { tab ->
        TickerTab(
            current = currentTab,
            tab = tab,
            onTabUpdated = onTabUpdated,
        )
      }
    }
  }
}

@Composable
private fun TickerTab(
    tab: EquityType,
    current: EquityType,
    onTabUpdated: (EquityType) -> Unit,
) {
  val contentColor = LocalContentColor.current

  Tab(
      selected = tab == current,
      selectedContentColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium),
      unselectedContentColor = contentColor.copy(alpha = ContentAlpha.medium),
      onClick = { onTabUpdated(tab) },
  ) {
    Text(
        modifier = Modifier.padding(vertical = MaterialTheme.keylines.typography),
        text = tab.display,
    )
  }
}

@Composable
private fun SearchInput(
    modifier: Modifier = Modifier,
    search: String,
    onSearchChanged: (String) -> Unit,
) {
  val (isSearchFocused, setSearchFocused) = remember { mutableStateOf(false) }

  val hasSearchQuery = remember(search) { search.isNotBlank() }
  val handleClearSearch by rememberUpdatedState { onSearchChanged("") }

  // If search bar is populated and focused, back gesture clears
  if (isSearchFocused && hasSearchQuery) {
    BackHandler(
        onBack = handleClearSearch,
    )
  }

  OutlinedTextField(
      modifier =
          modifier
              .padding(horizontal = MaterialTheme.keylines.baseline)
              .padding(bottom = MaterialTheme.keylines.baseline)
              .onFocusChanged { focus -> setSearchFocused(focus.isFocused) },
      value = search,
      onValueChange = onSearchChanged,
      singleLine = true,
      shape = RoundedCornerShape(percent = 50),
      label = {
        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.keylines.baseline),
            text = "Search for something...",
        )
      },
      trailingIcon = {
        AnimatedVisibility(visible = search.isNotBlank()) {
          IconButton(
              onClick = handleClearSearch,
          ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear Search",
            )
          }
        }
      },
  )
}

@Preview
@Composable
private fun PreviewSearchBar() {
  Surface {
    SearchBar(
        search = "",
        onSearchChanged = {},
        currentTab = EquityType.STOCK,
        onTabUpdated = {},
        onRegenerateList = {},
    )
  }
}
