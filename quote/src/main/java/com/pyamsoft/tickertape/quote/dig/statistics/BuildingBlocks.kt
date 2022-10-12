package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.HairlineSize
import com.pyamsoft.pydroid.theme.keylines

@Composable
internal fun StatisticsTitle(
    modifier: Modifier = Modifier,
    title: String,
    big: Boolean = false,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline),
        text = title,
        style = if (big) MaterialTheme.typography.h4 else MaterialTheme.typography.h6,
    )
  }
}

private val ITEM_HEIGHT = 48.dp

@Composable
internal fun StatisticsItem(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
) {
  val borderColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
  val border = remember(borderColor) { BorderStroke(HairlineSize, borderColor) }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier =
            Modifier.fillMaxWidth(fraction = 0.4F)
                .height(ITEM_HEIGHT)
                .border(border)
                .padding(MaterialTheme.keylines.baseline),
        text = title,
        style = MaterialTheme.typography.caption,
    )

    Text(
        modifier =
            Modifier.fillMaxWidth()
                .height(ITEM_HEIGHT)
                .border(border)
                .padding(MaterialTheme.keylines.baseline),
        text = content,
        style = MaterialTheme.typography.body1,
    )
  }
}
