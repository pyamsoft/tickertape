package com.pyamsoft.tickertape.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@JvmOverloads
fun SearchBar(
    modifier: Modifier = Modifier,
    search: String,
    onSearchChanged: (String) -> Unit,
) {
  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape = MaterialTheme.shapes.medium,
  ) {
    OutlinedTextField(
        modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp),
        value = search,
        onValueChange = onSearchChanged,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        label = {
          Text(
              text = "Search for something...",
          )
        },
        colors =
            TextFieldDefaults.outlinedTextFieldColors(
                leadingIconColor =
                    MaterialTheme.colors.onPrimary.copy(alpha = TextFieldDefaults.IconOpacity),
                unfocusedBorderColor =
                    MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.disabled),
                focusedBorderColor =
                    MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium),
                unfocusedLabelColor =
                    MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium),
                focusedLabelColor = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.high),
            ),
    )
  }
}

@Preview
@Composable
private fun PreviewSearchBar() {
  var search by remember { mutableStateOf("") }
  SearchBar(
      search = search,
      onSearchChanged = { search = it },
  )
}
