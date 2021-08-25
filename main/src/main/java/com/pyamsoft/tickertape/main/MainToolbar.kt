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

import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.google.android.material.R as R2
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivityProvider
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.util.doOnLayoutChanged
import com.pyamsoft.pydroid.util.doOnApplyWindowInsets
import com.pyamsoft.tickertape.core.PRIVACY_POLICY_URL
import com.pyamsoft.tickertape.core.TERMS_CONDITIONS_URL
import com.pyamsoft.tickertape.main.databinding.MainToolbarBinding
import com.pyamsoft.tickertape.ui.withRoundedBackground
import javax.inject.Inject
import javax.inject.Named

class MainToolbar
@Inject
internal constructor(
    @Named("app_name") appNameRes: Int,
    toolbarActivityProvider: ToolbarActivityProvider,
    theming: ThemeProvider,
    appBarProvider: AppBarActivityProvider,
    parent: ViewGroup,
) : BaseUiView<MainViewState, MainViewEvent, MainToolbarBinding>(parent) {

  private var titleAnimator: ViewPropertyAnimatorCompat? = null
  override val viewBinding = MainToolbarBinding::inflate

  override val layoutRoot by boundView { mainAppbar }

  init {
    doOnInflate { layoutRoot.outlineProvider = ViewOutlineProvider.BACKGROUND }

    doOnInflate { binding.mainToolbar.outlineProvider = null }

    doOnInflate {
      binding.mainAppbar.apply {
        appBarProvider.setAppBar(this)
        elevation = 0F
      }
    }

    doOnTeardown { appBarProvider.setAppBar(null) }

    doOnInflate {
      inflateToolbar(toolbarActivityProvider, theming, appNameRes)

      binding.mainAppbar
          .doOnApplyWindowInsets { v, insets, _ ->
            val toolbarTopMargin = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> { this.topMargin = toolbarTopMargin }
          }
          .also { doOnTeardown { it.cancel() } }
    }

    doOnTeardown {
      titleAnimator?.cancel()
      titleAnimator = null
    }

    doOnTeardown {
      toolbarActivityProvider.setToolbar(null)
    }

    doOnInflate {
      binding.mainAppbar
          .doOnLayoutChanged { v, _, _, _, _, _, _, _, _ ->
            binding.apply {
              mainAppbar.withRoundedBackground(applyAllCorners = true)
              mainToolbar.elevation = 0F
            }

            // Need to include the margin here when measuring top
            publish(MainViewEvent.TopBarMeasured(v.height + v.marginTop))
          }
          .also { doOnTeardown { it.cancel() } }
    }

    doOnInflate { binding.mainToolbar.inflateMenu(R.menu.toolbar) }

    doOnInflate {
      binding.mainToolbar.setOnMenuItemClickListener { item ->
        return@setOnMenuItemClickListener when (item.itemId) {
          R.id.menu_main_settings -> {
            publish(MainViewEvent.OpenSettings)
            true
          }
          else -> false
        }
      }
    }

    doOnTeardown { binding.mainToolbar.setOnMenuItemClickListener(null) }

    doOnTeardown { binding.mainToolbar.menu.removeItem(R.id.menu_main_settings) }

    doOnInflate { animateToolbar() }
  }

  private fun animateToolbar() {
    // this is gross but toolbar doesn't expose it's children to animate them :(
    val t = binding.mainToolbar.getChildAt(0)
    if (t is TextView) {
      t.apply {
        // Fade in and space out the title.
        alpha = 0F
        scaleX = 0.8F
        titleAnimator =
            ViewCompat.animate(this)
                .setInterpolator(FastOutLinearInInterpolator())
                .alpha(1F)
                .scaleX(1F)
                .setStartDelay(300)
                .setDuration(900)
      }
    }
  }

  private fun handleName(@StringRes name: Int) {
    if (name == 0) {
      binding.mainToolbar.title = null
    } else {
      binding.mainToolbar.setTitle(name)
    }
  }

  override fun onRender(state: UiRender<MainViewState>) {
    state.mapChanged { it.appNameRes }.render(viewScope) { handleName(it) }
  }

  private fun inflateToolbar(
      toolbarActivityProvider: ToolbarActivityProvider,
      theming: ThemeProvider,
      appNameRes: Int,
  ) {
    val theme =
        if (theming.isDarkTheme()) {
          R2.style.ThemeOverlay_MaterialComponents
        } else {
          R2.style.ThemeOverlay_MaterialComponents_Light
        }

    binding.mainToolbar.apply {
      popupTheme = theme
      ViewCompat.setElevation(this, 0F)
      setTitle(appNameRes)
      toolbarActivityProvider.setToolbar(this)
    }
  }
}
