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

package com.pyamsoft.tickertape.watchlist.dig

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.ImageTarget
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.util.tintWith
import com.pyamsoft.tickertape.main.databinding.SymbolAddToolbarBinding
import com.pyamsoft.tickertape.ui.withRoundedBackground
import javax.inject.Inject

class WatchlistDigToolbar
@Inject
internal constructor(
    imageLoader: ImageLoader,
    parent: ViewGroup,
) : BaseUiView<WatchListDigViewState, WatchListDigViewEvent, SymbolAddToolbarBinding>(parent) {

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

    doOnInflate { binding.symbolAddToolbar.withRoundedBackground() }

    doOnInflate {
      binding.symbolAddToolbar.setNavigationOnClickListener(
          DebouncedOnClickListener.create { publish(WatchListDigViewEvent.Close) })
    }

    doOnTeardown { clear() }
  }

  private fun clear() {
    binding.symbolAddToolbar.setNavigationOnClickListener(null)
  }
}
