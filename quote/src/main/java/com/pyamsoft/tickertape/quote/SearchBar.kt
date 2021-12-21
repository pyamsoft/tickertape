package com.pyamsoft.tickertape.quote

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@JvmOverloads
fun SearchBar(
    modifier: Modifier = Modifier,
    search: String,
    currentTab: TickerTabs,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (TickerTabs) -> Unit,
) {
  Surface(
      modifier = modifier.padding(horizontal = 8.dp),
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape = MaterialTheme.shapes.medium,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      SearchInput(
          modifier = Modifier.fillMaxWidth(),
          search = search,
          onSearchChanged = onSearchChanged,
      )

      TabRow(
          backgroundColor = Color.Transparent,
          selectedTabIndex = currentTab.ordinal,
      ) {
        TickerTabs.values().forEach { tab ->
          TickerTab(
              current = currentTab,
              tab = tab,
              onTabUpdated = onTabUpdated,
          )
        }
      }
    }
  }
}

@Composable
private fun TickerTab(
    tab: TickerTabs,
    current: TickerTabs,
    onTabUpdated: (TickerTabs) -> Unit,
) {
  Tab(
      selected = tab == current,
      onClick = { onTabUpdated(tab) },
  ) {
    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = tab.display,
    )
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun SearchInput(
    modifier: Modifier = Modifier,
    search: String,
    onSearchChanged: (String) -> Unit,
) {
  OutlinedTextField(
      modifier = modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp),
      value = search,
      onValueChange = onSearchChanged,
      singleLine = true,
      shape = MaterialTheme.shapes.medium,
      label = {
        Text(
            text = "Search for something...",
        )
      },
      trailingIcon = {
        AnimatedVisibility(visible = search.isNotBlank()) {
          IconButton(
              onClick = { onSearchChanged("") },
          ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear Search",
            )
          }
        }
      },
      colors =
          TextFieldDefaults.outlinedTextFieldColors(
              leadingIconColor =
                  MaterialTheme.colors.onPrimary.copy(alpha = TextFieldDefaults.IconOpacity),
              unfocusedBorderColor =
                  MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.disabled),
              focusedBorderColor = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium),
              unfocusedLabelColor =
                  MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium),
              focusedLabelColor = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.high),
          ),
  )
}

@Preview
@Composable
private fun PreviewSearchBar() {
  SearchBar(
      search = "",
      onSearchChanged = {},
      currentTab = TickerTabs.STOCKS,
      onTabUpdated = {},
  )
}
