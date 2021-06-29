/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.tickertape.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.CheckResult
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.pyamsoft.pydroid.util.asDp

@CheckResult
@JvmOverloads
fun createRoundedBackground(
    context: Context,
    @ColorRes colorRes: Int = R.color.blue500,
    applyAllCorners: Boolean = false
): Drawable {
  val cornerSize = 16.asDp(context).toFloat()

  val shapeModel =
      ShapeAppearanceModel.Builder()
          .apply {
            if (applyAllCorners) {
              setAllCorners(RoundedCornerTreatment())
              setAllCornerSizes(cornerSize)
            } else {
              setTopRightCorner(RoundedCornerTreatment())
              setTopLeftCorner(RoundedCornerTreatment())
              setTopRightCornerSize(cornerSize)
              setTopLeftCornerSize(cornerSize)
            }
          }
          .build()

  // Create background
  val color = ContextCompat.getColor(context, colorRes)
  return MaterialShapeDrawable(shapeModel).apply {
    initializeElevationOverlay(context)
    shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
    fillColor = ColorStateList.valueOf(color)
    elevation = 0F
  }
}

@JvmOverloads
fun View.withRoundedBackground(
    @ColorRes color: Int = R.color.blue500,
    applyAllCorners: Boolean = false
) {
  val ctx = context.applicationContext
  this.background = createRoundedBackground(ctx, color, applyAllCorners)
  this.elevation = 8.asDp(ctx).toFloat()
}
