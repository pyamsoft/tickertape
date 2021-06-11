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

package com.pyamsoft.tickertape.watchlist.add

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.ImageTarget
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.pydroid.util.tintWith
import com.pyamsoft.tickertape.watchlist.databinding.SymbolAddToolbarBinding
import javax.inject.Inject

class SymbolToolbar
@Inject
internal constructor(
    imageLoader: ImageLoader,
    parent: ViewGroup,
) : BaseUiView<SymbolAddViewState, SymbolAddViewEvent, SymbolAddToolbarBinding>(parent) {

  override val viewBinding = SymbolAddToolbarBinding::inflate

  override val layoutRoot by boundView { symbolAddToolbar }

  init {
    doOnInflate {
      imageLoader
          .asDrawable()
          .load(R.drawable.ic_close_24dp)
          .mutate { it.tintWith(Color.WHITE) }
          .into(
              object : ImageTarget<Drawable> {

                override fun clear() {
                  binding.symbolAddToolbar.navigationIcon = null
                }

                override fun setImage(image: Drawable) {
                  binding.symbolAddToolbar.setUpEnabled(true, image)
                }
              })
          .also { loaded -> doOnTeardown { loaded.dispose() } }
    }

    doOnInflate {
      val context = layoutRoot.context
      val cornerSize = 16.asDp(layoutRoot.context).toFloat()

      val shapeModel =
          ShapeAppearanceModel.Builder()
              .apply {
                setTopRightCorner(RoundedCornerTreatment())
                setTopLeftCorner(RoundedCornerTreatment())
                setTopRightCornerSize(cornerSize)
                setTopLeftCornerSize(cornerSize)
              }
              .build()

      // Create background
      val color = ContextCompat.getColor(context, R.color.blue500)
      val materialShapeDrawable = MaterialShapeDrawable(shapeModel)
      materialShapeDrawable.initializeElevationOverlay(context)
      materialShapeDrawable.shadowCompatibilityMode =
          MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
      materialShapeDrawable.fillColor = ColorStateList.valueOf(color)
      materialShapeDrawable.elevation = 0F

      binding.symbolAddToolbar.apply {
        elevation = 8.asDp(context).toFloat()
        background = materialShapeDrawable
      }
    }

    doOnInflate {
      binding.symbolAddToolbar.setNavigationOnClickListener(
          DebouncedOnClickListener.create { publish(SymbolAddViewEvent.Close) })
    }

    doOnTeardown { clear() }
  }

  private fun clear() {
    binding.symbolAddToolbar.setNavigationOnClickListener(null)
  }
}
