package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pyamsoft.pydroid.theme.keylines

@Composable
internal fun StatisticsTitle(
    modifier: Modifier = Modifier,
    title: String,
    big: Boolean = false,
) {
  Text(
      modifier = modifier.padding(MaterialTheme.keylines.baseline),
      text = title,
      textAlign = TextAlign.Center,
      style = if (big) MaterialTheme.typography.h4 else MaterialTheme.typography.h6,
  )
}

@Composable
internal fun StatisticsItem(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
) {

  val label = remember(title) { title.uppercase() }

  Column(
      modifier = modifier.padding(bottom = MaterialTheme.keylines.baseline),
  ) {
    Text(
        text = label,
        style =
            MaterialTheme.typography.overline.copy(
                fontWeight = FontWeight.W400,
                color =
                    MaterialTheme.colors.onBackground.copy(
                        alpha = ContentAlpha.medium,
                    ),
            ),
    )

    Text(
        text = content,
        style =
            MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.W700,
                color =
                    MaterialTheme.colors.onBackground.copy(
                        alpha = ContentAlpha.high,
                    ),
            ),
    )
  }
}
