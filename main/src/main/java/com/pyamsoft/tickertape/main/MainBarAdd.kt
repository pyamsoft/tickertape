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

package com.pyamsoft.tickertape.main

import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.main.databinding.MainAddBinding
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.ui.R
import javax.inject.Inject

class MainBarAdd
@Inject
internal constructor(
    private val imageLoader: ImageLoader,
    parent: ViewGroup,
) : BaseUiView<MainViewState, MainViewEvent, MainAddBinding>(parent) {

  override val viewBinding = MainAddBinding::inflate

  override val layoutRoot by boundView { mainBarAdd }

  private val interpolator by lazy(LazyThreadSafetyMode.NONE) { LinearInterpolator() }
  private var animator: ViewPropertyAnimatorCompat? = null

  init {
    doOnInflate {
      binding.apply {
        mainBarAdd.setOnDebouncedClickListener { publish(MainViewEvent.AddRequest) }
        mainBarAddStock.setOnDebouncedClickListener {
          publish(MainViewEvent.OpenAdd(HoldingType.Stock))
        }
        mainBarAddOptions.setOnDebouncedClickListener {
          publish(MainViewEvent.OpenAdd(HoldingType.Options.Buy))
        }
        mainBarAddCrypto.setOnDebouncedClickListener {
          publish(MainViewEvent.OpenAdd(HoldingType.Crypto))
        }
      }
    }

    doOnInflate {
      imageLoader.asDrawable().load(R.drawable.ic_add_24dp).into(binding.mainBarAdd).also { l ->
        doOnTeardown { l.dispose() }
      }
    }

    doOnTeardown {
      binding.apply {
        mainBarAdd.setOnDebouncedClickListener(null)
        mainBarAddStock.setOnDebouncedClickListener(null)
        mainBarAddOptions.setOnDebouncedClickListener(null)
        mainBarAddCrypto.setOnDebouncedClickListener(null)
      }
    }

    doOnTeardown {
      animator?.cancel()
      animator = null
    }

    doOnTeardown {
      binding.apply {
        mainBarAddStock.isGone = true
        mainBarAddOptions.isGone = true
        mainBarAddCrypto.isGone = true
      }
    }
  }

  override fun onRender(state: UiRender<MainViewState>) {
    state.mapChanged { it.page }.mapChanged { it.isFabVisible() }.render(viewScope) {
      handleFabVisible(it)
    }
    state.mapChanged { it.adding }.render(viewScope) { handleAddingChanged(it) }
  }

  private fun handleAddingChanged(adding: Boolean) {
    handleShowAddingButtons(adding)
    handleRotateFab(adding)
  }

  private fun View.showHideView(adding: Boolean) {
    if (adding) {
      isVisible = true
    } else {
      isGone = true
    }
  }

  private fun handleShowAddingButtons(adding: Boolean) {
    binding.apply {
      mainBarAddStock.showHideView(adding)
      mainBarAddOptions.showHideView(adding)
      mainBarAddCrypto.showHideView(adding)
    }
  }

  private fun handleRotateFab(adding: Boolean) {
    animator?.cancel()
    animator =
        ViewCompat.animate(binding.mainBarAdd)
        .setDuration(ANIMATION_DURATION)
        .setInterpolator(interpolator)
        .rotation(if (adding) 45F else 0F)
        .apply { start() }
  }

  private fun handleFabVisible(visible: Boolean) {
    binding.mainBarAdd.apply { if (visible) show() else hide() }
  }

  companion object {

    private const val ANIMATION_DURATION = 150L
  }
}
