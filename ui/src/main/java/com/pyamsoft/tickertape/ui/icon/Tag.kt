package com.pyamsoft.tickertape.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied from material-icons-extended
@Suppress("unused")
val Icons.Filled.Tag: ImageVector
  get() {
    if (_tag != null) {
      return _tag!!
    }
    _tag =
        materialIcon(name = "Filled.Tag") {
          materialPath {
            moveTo(20.0f, 10.0f)
            lineTo(20.0f, 8.0f)
            horizontalLineToRelative(-4.0f)
            lineTo(16.0f, 4.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(4.0f)
            horizontalLineToRelative(-4.0f)
            lineTo(10.0f, 4.0f)
            lineTo(8.0f, 4.0f)
            verticalLineToRelative(4.0f)
            lineTo(4.0f, 8.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(4.0f)
            lineTo(4.0f, 14.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(4.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-4.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(4.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-4.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(-4.0f)
            horizontalLineToRelative(4.0f)
            close()
            moveTo(14.0f, 14.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(-4.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(4.0f)
            close()
          }
        }
    return _tag!!
  }

@Suppress("ObjectPropertyName") private var _tag: ImageVector? = null
