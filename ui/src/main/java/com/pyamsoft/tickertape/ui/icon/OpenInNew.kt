package com.pyamsoft.tickertape.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("unused")
val Icons.Filled.OpenInNew: ImageVector
  get() {
    if (_openInNew != null) {
      return _openInNew!!
    }
    _openInNew =
        materialIcon(name = "Filled.OpenInNew") {
          materialPath {
            moveTo(19.0f, 19.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(7.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(5.0f)
            curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-7.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(7.0f)
            close()
            moveTo(14.0f, 3.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(3.59f)
            lineToRelative(-9.83f, 9.83f)
            lineToRelative(1.41f, 1.41f)
            lineTo(19.0f, 6.41f)
            verticalLineTo(10.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(-7.0f)
            close()
          }
        }
    return _openInNew!!
  }

@Suppress("ObjectPropertyName") private var _openInNew: ImageVector? = null
