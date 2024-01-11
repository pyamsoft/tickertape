/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied from material-icons-extended
@Suppress("unused")
val Icons.Filled.Today: ImageVector
  get() {
    if (_today != null) {
      return _today!!
    }
    _today =
        materialIcon(name = "Filled.Today") {
          materialPath {
            moveTo(19.0f, 3.0f)
            horizontalLineToRelative(-1.0f)
            lineTo(18.0f, 1.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            lineTo(8.0f, 3.0f)
            lineTo(8.0f, 1.0f)
            lineTo(6.0f, 1.0f)
            verticalLineToRelative(2.0f)
            lineTo(5.0f, 3.0f)
            curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
            lineTo(3.0f, 19.0f)
            curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            lineTo(21.0f, 5.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(19.0f, 19.0f)
            lineTo(5.0f, 19.0f)
            lineTo(5.0f, 8.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(11.0f)
            close()
            moveTo(7.0f, 10.0f)
            horizontalLineToRelative(5.0f)
            verticalLineToRelative(5.0f)
            lineTo(7.0f, 15.0f)
            close()
          }
        }
    return _today!!
  }

@Suppress("ObjectPropertyName") private var _today: ImageVector? = null
