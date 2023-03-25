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
val Icons.Filled.Paid: ImageVector
  get() {
    if (_paid != null) {
      return _paid!!
    }
    _paid =
        materialIcon(name = "Filled.Paid") {
          materialPath {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(12.88f, 17.76f)
            verticalLineTo(19.0f)
            horizontalLineToRelative(-1.75f)
            verticalLineToRelative(-1.29f)
            curveToRelative(-0.74f, -0.18f, -2.39f, -0.77f, -3.02f, -2.96f)
            lineToRelative(1.65f, -0.67f)
            curveToRelative(0.06f, 0.22f, 0.58f, 2.09f, 2.4f, 2.09f)
            curveToRelative(0.93f, 0.0f, 1.98f, -0.48f, 1.98f, -1.61f)
            curveToRelative(0.0f, -0.96f, -0.7f, -1.46f, -2.28f, -2.03f)
            curveToRelative(-1.1f, -0.39f, -3.35f, -1.03f, -3.35f, -3.31f)
            curveToRelative(0.0f, -0.1f, 0.01f, -2.4f, 2.62f, -2.96f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(1.75f)
            verticalLineToRelative(1.24f)
            curveToRelative(1.84f, 0.32f, 2.51f, 1.79f, 2.66f, 2.23f)
            lineToRelative(-1.58f, 0.67f)
            curveToRelative(-0.11f, -0.35f, -0.59f, -1.34f, -1.9f, -1.34f)
            curveToRelative(-0.7f, 0.0f, -1.81f, 0.37f, -1.81f, 1.39f)
            curveToRelative(0.0f, 0.95f, 0.86f, 1.31f, 2.64f, 1.9f)
            curveToRelative(2.4f, 0.83f, 3.01f, 2.05f, 3.01f, 3.45f)
            curveTo(15.9f, 17.17f, 13.4f, 17.67f, 12.88f, 17.76f)
            close()
          }
        }
    return _paid!!
  }

@Suppress("ObjectPropertyName") private var _paid: ImageVector? = null
