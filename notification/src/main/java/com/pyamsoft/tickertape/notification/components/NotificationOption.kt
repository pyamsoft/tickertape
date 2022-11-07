package com.pyamsoft.tickertape.notification.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun NotificationOption(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    title: String,
    description: String,
) {
  val color = if (isEnabled) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

  Column(
      modifier = modifier,
  ) {
    Text(
        text = title,
        style =
            MaterialTheme.typography.body1.copy(
                color =
                    color.copy(
                        alpha = if (isEnabled) ContentAlpha.high else ContentAlpha.medium,
                    ),
            ),
    )

    Text(
        text = description,
        style =
            MaterialTheme.typography.caption.copy(
                color =
                    MaterialTheme.colors.onSurface.copy(
                        alpha = if (isEnabled) ContentAlpha.medium else ContentAlpha.disabled,
                    ),
            ),
    )
  }
}
