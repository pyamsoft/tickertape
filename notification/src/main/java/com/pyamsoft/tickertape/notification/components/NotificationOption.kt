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
