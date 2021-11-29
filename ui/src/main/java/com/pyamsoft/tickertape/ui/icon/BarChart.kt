package com.pyamsoft.tickertape.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied from material-icons-extended
@Suppress("unused")
val Icons.Filled.BarChart: ImageVector
  get() {
    if (_barChart != null) {
      return _barChart!!
    }
    _barChart =
        materialIcon(name = "Filled.BarChart") {
          materialPath {
            moveTo(5.0f, 9.2f)
            horizontalLineToRelative(3.0f)
            lineTo(8.0f, 19.0f)
            lineTo(5.0f, 19.0f)
            close()
            moveTo(10.6f, 5.0f)
            horizontalLineToRelative(2.8f)
            verticalLineToRelative(14.0f)
            horizontalLineToRelative(-2.8f)
            close()
            moveTo(16.2f, 13.0f)
            lineTo(19.0f, 13.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(-2.8f)
            close()
          }
        }
    return _barChart!!
  }

@Suppress("ObjectPropertyName") private var _barChart: ImageVector? = null
