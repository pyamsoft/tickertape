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

package com.pyamsoft.tickertape.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied from material-icons-extended
@Suppress("unused")
val Icons.Filled.PieChart: ImageVector
  get() {
    if (_pieChart != null) {
      return _pieChart!!
    }
    _pieChart =
        materialIcon(name = "Filled.PieChart") {
          materialPath {
            moveTo(11.0f, 2.0f)
            verticalLineToRelative(20.0f)
            curveToRelative(-5.07f, -0.5f, -9.0f, -4.79f, -9.0f, -10.0f)
            reflectiveCurveToRelative(3.93f, -9.5f, 9.0f, -10.0f)
            close()
            moveTo(13.03f, 2.0f)
            verticalLineToRelative(8.99f)
            lineTo(22.0f, 10.99f)
            curveToRelative(-0.47f, -4.74f, -4.24f, -8.52f, -8.97f, -8.99f)
            close()
            moveTo(13.03f, 13.01f)
            lineTo(13.03f, 22.0f)
            curveToRelative(4.74f, -0.47f, 8.5f, -4.25f, 8.97f, -8.99f)
            horizontalLineToRelative(-8.97f)
            close()
          }
        }
    return _pieChart!!
  }

@Suppress("ObjectPropertyName") private var _pieChart: ImageVector? = null
