package com.pyamsoft.tickertape.ui

import androidx.annotation.CheckResult
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.pyamsoft.pydroid.theme.HairlineSize

enum class Borders {
  LEFT,
  RIGHT,
  TOP,
  BOTTOM
}

@Composable
@CheckResult
fun Modifier.drawBorder(border: Borders): Modifier {
  val density = LocalDensity.current
  val borderColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
  return this.drawBorder(density, borderColor, border)
}

@CheckResult
fun Modifier.drawBorder(
    density: Density,
    color: Color,
    border: Borders,
): Modifier {
  val self = this
  val strokeWidth = (density.run { HairlineSize.toPx() } / 2)

  return self.drawBehind {
    val drawScope = this

    val start: Offset
    val end: Offset
    when (border) {
      Borders.LEFT -> {
        start = Offset(0F, drawScope.size.height)
        end = Offset(0F, 0F)
      }
      Borders.RIGHT -> {
        val x = drawScope.size.width
        start = Offset(x, drawScope.size.height)
        end = Offset(x, 0F)
      }
      Borders.TOP -> {
        val y = drawScope.size.height
        start = Offset(0F, y)
        end = Offset(drawScope.size.width, y)
      }
      Borders.BOTTOM -> {
        start = Offset(0F, 0F)
        end = Offset(drawScope.size.width, 0F)
      }
    }

    drawScope.drawLine(color, start, end, strokeWidth)
  }
}
