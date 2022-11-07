package com.pyamsoft.tickertape.notification.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.ui.icon.RadioButtonUnchecked

enum class NotificationCardSize {
  SMALL,
  LARGE,
}

@Composable
internal fun ColoredCard(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
  val color = if (isChecked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
  val mediumAlpha =
      if (isEnabled) if (isChecked) ContentAlpha.high else ContentAlpha.medium
      else ContentAlpha.disabled

  Box(
      modifier =
          modifier.border(
              width = 2.dp,
              color = color.copy(alpha = mediumAlpha),
              shape = MaterialTheme.shapes.medium,
          ),
  ) {
    Card(
        elevation = CardDefaults.Elevation,
    ) {
      content()
    }
  }
}

@Composable
internal fun NotificationCard(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    title: String,
    size: NotificationCardSize,
    onCheckedChanged: (Boolean) -> Unit,
    contentDescription: String? = null,
    contentDescriptionOn: String? = null,
    contentDescriptionOff: String? = null,
    content: @Composable () -> Unit = {},
) {
  val textColor = MaterialTheme.colors.onSurface
  val color = if (isChecked) MaterialTheme.colors.primary else textColor
  // High alpha when selected
  val mediumAlpha = if (isChecked) ContentAlpha.high else ContentAlpha.medium

  val description =
      rememberContentDescription(
          isChecked,
          contentDescription,
          contentDescriptionOn,
          contentDescriptionOff,
      )

  val typography = MaterialTheme.typography
  val highContentAlpha = ContentAlpha.high

  val titleStyle =
      remember(
          typography,
          highContentAlpha,
          size,
          color,
      ) {
        val style = if (size == NotificationCardSize.LARGE) typography.h6 else typography.body2
        return@remember style.copy(
            color = color.copy(alpha = highContentAlpha),
            fontWeight = FontWeight.W700,
        )
      }

  val descriptionStyle =
      remember(
          typography,
          mediumAlpha,
          size,
          textColor,
      ) {
        val style = if (size == NotificationCardSize.LARGE) typography.body2 else typography.caption
        return@remember style.copy(
            color = textColor.copy(alpha = mediumAlpha),
            fontWeight = FontWeight.W400,
        )
      }

  ColoredCard(
      modifier = modifier,
      isChecked = isChecked,
  ) {
    Column(
        modifier =
            Modifier.clickable { onCheckedChanged(!isChecked) }
                .padding(MaterialTheme.keylines.content),
    ) {
      Row {
        Icon(
            imageVector =
                if (isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = description,
            tint = color.copy(alpha = ContentAlpha.high),
        )

        Column(
            modifier = Modifier.padding(start = MaterialTheme.keylines.content),
        ) {
          Text(
              text = title,
              style = titleStyle,
          )

          if (description != null && description.isNotBlank()) {
            Text(
                text = description,
                style = descriptionStyle,
            )
          }
        }
      }

      content()
    }
  }
}
