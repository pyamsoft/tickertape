package com.pyamsoft.tickertape.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.ui.defaults.CardDefaults

@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colors.primary,
    content: @Composable () -> Unit,
) {
  Box(
      modifier =
          modifier.border(
              width = 2.dp,
              color = borderColor.copy(alpha = ContentAlpha.disabled),
              shape = MaterialTheme.shapes.medium,
          ),
  ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.Elevation,
    ) {
      content()
    }
  }
}

@Preview
@Composable
private fun PreviewBorderCard() {
  BorderCard {
    Text(
        text = "Just Testing",
        style = MaterialTheme.typography.h6,
    )
  }
}
