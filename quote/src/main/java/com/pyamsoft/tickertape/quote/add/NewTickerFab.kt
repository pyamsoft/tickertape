package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

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
  Surface {
    NewTickerFab(
        visible = true,
        onClick = {},
    )
  }
}
