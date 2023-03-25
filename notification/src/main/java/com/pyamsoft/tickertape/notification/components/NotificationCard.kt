/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
          color,
      ) {
        return@remember typography.h6.copy(
            color = color.copy(alpha = highContentAlpha),
            fontWeight = FontWeight.W700,
        )
      }

  val descriptionStyle =
      remember(
          typography,
          mediumAlpha,
          textColor,
      ) {
        return@remember typography.body2.copy(
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
