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

package com.pyamsoft.tickertape.portfolio.manage.positions.add

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.ImageTarget
import com.pyamsoft.pydroid.loader.Loaded
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.util.tintWith
import com.pyamsoft.tickertape.portfolio.databinding.ManagePortfolioToolbarBinding
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.withRoundedBackground
import javax.inject.Inject

class PositionAddToolbar
@Inject
internal constructor(
    private val imageLoader: ImageLoader,
    parent: ViewGroup,
) :
    BaseUiView<PositionsAddViewState, PositionsAddViewEvent, ManagePortfolioToolbarBinding>(
        parent) {

  override val viewBinding = ManagePortfolioToolbarBinding::inflate

  override val layoutRoot by boundView { positionToolbar }

  private var customImageLoaded: Loaded? = null

  init {
    doOnInflate { binding.positionToolbar.withRoundedBackground() }

    doOnInflate {
      binding.positionToolbar.setNavigationOnClickListener(
          DebouncedOnClickListener.create { publish(PositionsAddViewEvent.Close) })
    }

    doOnTeardown { binding.positionToolbar.setNavigationOnClickListener(null) }

    doOnInflate { loadCustomImage() }
    doOnTeardown { unloadImage() }

    doOnTeardown { binding.positionToolbar.title = "" }
  }

  private fun loadCustomImage() {
    unloadImage()
    customImageLoaded =
        imageLoader
            .asDrawable()
            .load(R.drawable.ic_close_24dp)
            .mutate { it.tintWith(Color.WHITE) }
            .into(
                object : ImageTarget<Drawable> {

                  override fun clear() {
                    binding.positionToolbar.navigationIcon = null
                  }

                  override fun setImage(image: Drawable) {
                    binding.positionToolbar.setUpEnabled(true, image)
                  }
                })
  }

  private fun unloadImage() {
    customImageLoaded?.dispose()
    customImageLoaded = null
  }

  override fun onRender(state: UiRender<PositionsAddViewState>) {
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbol(it) }
  }

  private fun handleSymbol(symbol: StockSymbol) {
    binding.positionToolbar.title = symbol.symbol()
  }
}
