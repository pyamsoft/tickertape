package com.pyamsoft.tickertape.alert.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.ui.icon.RadioButtonUnchecked

@Composable
internal fun AlertCard(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    title: String,
    onCheckedChanged: (Boolean) -> Unit,
    contentDescription: String? = null,
    contentDescriptionOn: String? = null,
    contentDescriptionOff: String? = null,
    content: @Composable () -> Unit = {},
) {
  val color = if (isChecked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
  // High alpha when selected
  val mediumAlpha = if (isChecked) ContentAlpha.high else ContentAlpha.medium

  val description =
      rememberContentDescription(
          isChecked,
          contentDescription,
          contentDescriptionOn,
          contentDescriptionOff,
      )

  Box(
      modifier =
          modifier.border(
              width = 2.dp,
              color = color.copy(alpha = mediumAlpha),
              shape = MaterialTheme.shapes.medium,
          ),
  ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.Elevation,
    ) {
      Column(
          modifier =
              Modifier.fillMaxWidth()
                  .clickable { onCheckedChanged(!isChecked) }
                  .padding(MaterialTheme.keylines.content),
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
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
                style =
                    MaterialTheme.typography.body1.copy(
                        color = color.copy(alpha = ContentAlpha.high),
                        fontWeight = FontWeight.W700,
                    ),
            )

            if (description != null && description.isNotBlank()) {
              Text(
                  text = description,
                  style =
                      MaterialTheme.typography.caption.copy(
                          color = MaterialTheme.colors.onSurface.copy(alpha = mediumAlpha),
                          fontWeight = FontWeight.W400,
                      ),
              )
            }
          }
        }

        content()
      }
    }
  }
}
