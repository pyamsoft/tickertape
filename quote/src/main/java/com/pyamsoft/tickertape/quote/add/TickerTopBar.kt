package com.pyamsoft.tickertape.quote.add

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun TickerAddTopBar(
    modifier: Modifier = Modifier,
    hasEquitySelection: Boolean,
    onClose: () -> Unit,
) {
  val topBarBackgroundColor: Color
  val topBarContentColor: Color
  val navigationIcon: ImageVector
  if (hasEquitySelection) {
    topBarBackgroundColor = MaterialTheme.colors.primary
    topBarContentColor = MaterialTheme.colors.onPrimary
    navigationIcon = Icons.Filled.ArrowBack
  } else {
    topBarBackgroundColor = MaterialTheme.colors.surface
    topBarContentColor = MaterialTheme.colors.onSurface
    navigationIcon = Icons.Filled.Close
  }

  TopAppBar(
      modifier = modifier,
      backgroundColor = topBarBackgroundColor,
      contentColor = topBarContentColor,
      title = {},
      navigationIcon = {
        IconButton(
            onClick = onClose,
        ) {
          Icon(
              imageVector = navigationIcon,
              contentDescription = "Close",
          )
        }
      },
  )
}

@Composable
private fun PreviewTickerTopBar(
    hasEquitySelection: Boolean,
) {
  TickerAddTopBar(
      hasEquitySelection = hasEquitySelection,
      onClose = {},
  )
}

@Preview
@Composable
private fun PreviewTickerTopBarNoSelection() {
  PreviewTickerTopBar(
      hasEquitySelection = false,
  )
}

@Preview
@Composable
private fun PreviewTickerTopBarWithSelection() {
  PreviewTickerTopBar(
      hasEquitySelection = true,
  )
}
