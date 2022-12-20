package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.pyamsoft.pydroid.theme.ZeroSize
import com.pyamsoft.pydroid.theme.keylines

interface QuoteScope {

  @Composable fun Info(value: String)

  @Composable
  fun Info(
      modifier: Modifier,
      value: String,
  )

  @Composable
  fun Info(
      name: String,
      value: String,
  )

  @Composable
  fun Info(
      modifier: Modifier,
      name: String,
      value: String,
  )

  @Composable
  fun Info(
      modifier: Modifier,
      name: String,
      value: String,
      nameColor: Color,
      valueColor: Color,
  )
}

object DefaultQuoteScopeInstance : QuoteScope {

  @Composable
  override fun Info(value: String) =
      Info(
          modifier = Modifier,
          value = value,
      )

  @Composable
  override fun Info(
      modifier: Modifier,
      value: String,
  ) =
      Info(
          modifier = modifier,
          name = "",
          value = value,
      )

  @Composable
  override fun Info(
      name: String,
      value: String,
  ) =
      Info(
          modifier = Modifier,
          name = name,
          value = value,
      )

  @Composable
  override fun Info(
      modifier: Modifier,
      name: String,
      value: String,
  ) {
    val color = MaterialTheme.colors.onSurface

    Info(
        modifier = modifier,
        name = name,
        value = value,
        nameColor = color,
        valueColor = color,
    )
  }

  @Composable
  override fun Info(
      modifier: Modifier,
      name: String,
      value: String,
      nameColor: Color,
      valueColor: Color,
  ) {
    val typography = MaterialTheme.typography

    val highAlpha = ContentAlpha.high
    val disabledAlpha = ContentAlpha.disabled

    val labelStyle =
        remember(
            typography,
            nameColor,
            disabledAlpha,
        ) {
          typography.overline.copy(
              color = nameColor.copy(alpha = disabledAlpha),
              fontWeight = FontWeight.W400,
          )
        }

    val contentStyle =
        remember(
            typography,
            valueColor,
            highAlpha,
        ) {
          typography.body1.copy(
              color = valueColor.copy(alpha = highAlpha),
              fontWeight = FontWeight.W400,
          )
        }

    val label = remember(name) { name.uppercase() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
      if (label.isNotBlank()) {
        Text(
            text = label,
            style = labelStyle,
            maxLines = 1,
        )
      }

      if (value.isNotBlank()) {
        Text(
            modifier =
                Modifier.padding(
                    // Only add bottom padding if we are a full Info
                    bottom = if (label.isNotBlank()) MaterialTheme.keylines.baseline else ZeroSize,
                ),
            text = value,
            style = contentStyle,
            maxLines = 1,
        )
      }
    }
  }
}
