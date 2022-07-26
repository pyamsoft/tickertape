package com.pyamsoft.tickertape.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.Card

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.secondary,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit
) {
  Card(
      modifier = modifier,
      shape = shape,
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      border = border,
      elevation = elevation,
      content = content,
  )
}

@Preview
@Composable
private fun PreviewCard() {
  PreviewTickerTapeTheme {
    ThemedCard(modifier = Modifier.padding(8.dp)) {
      Text(
          modifier = Modifier.padding(16.dp),
          text = "This is a Preview",
          style = MaterialTheme.typography.body2,
      )
    }
  }
}
