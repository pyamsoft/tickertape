package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme

@Composable
@JvmOverloads
fun NewTickerFab(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClick: () -> Unit,
) {
  AnimatedVisibility(
      modifier = modifier,
      visible = visible,
  ) {
    FloatingActionButton(
        backgroundColor = MaterialTheme.colors.primary,
        onClick = onClick,
    ) {
      Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = "New Ticker",
      )
    }
  }
}

@Preview
@Composable
private fun PreviewNewTickerFab() {
  PreviewTickerTapeTheme {
    Surface {
      NewTickerFab(
          modifier = Modifier.padding(8.dp),
          visible = true,
          onClick = {},
      )
    }
  }
}
