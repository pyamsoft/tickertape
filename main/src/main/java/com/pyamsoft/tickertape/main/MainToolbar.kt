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
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.R as R2
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivityProvider
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.privacy.addPrivacy
import com.pyamsoft.pydroid.ui.privacy.removePrivacy
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.util.asDp
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
    owner: LifecycleOwner,
    toolbarActivityProvider: ToolbarActivityProvider,
    theming: ThemeProvider,
    appBarProvider: AppBarActivityProvider,
    parent: ViewGroup,
) : BaseUiView<MainViewState, MainViewEvent, MainToolbarBinding>(parent) {

  private var titleAnimator: ViewPropertyAnimatorCompat? = null
  override val viewBinding = MainToolbarBinding::inflate

  override val layoutRoot by boundView { mainAppbar }

  init {
    doOnInflate { binding.mainAppbar.outlineProvider = ViewOutlineProvider.BACKGROUND }

    doOnInflate {
      binding.mainAppbar.apply {
        appBarProvider.setAppBar(this)
        elevation = 0F
      }
    }

    doOnTeardown { appBarProvider.setAppBar(null) }

    doOnInflate {
      inflateToolbar(toolbarActivityProvider, theming, appNameRes)

      layoutRoot.doOnApplyWindowInsets(owner) { v, insets, padding ->
        v.updateLayoutParams<MarginLayoutParams> {
          topMargin =
              padding.top +
                  insets.getInsets(WindowInsetsCompat.Type.systemBars()).top +
                  8.asDp(v.context)
        }
      }

      binding.mainToolbar.addPrivacy(viewScope, PRIVACY_POLICY_URL, TERMS_CONDITIONS_URL)
    }

    doOnInflate { animateToolbar() }

    doOnTeardown {
      titleAnimator?.cancel()
      titleAnimator = null
    }

    doOnTeardown {
      binding.mainToolbar.removePrivacy()
      toolbarActivityProvider.setToolbar(null)
    }

    doOnInflate {
      val listener =
          View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.mainAppbar.withRoundedBackground(applyAllCorners = true)
            binding.mainToolbar.elevation = 0F
          }
      binding.mainAppbar.addOnLayoutChangeListener(listener)

      doOnTeardown { binding.mainAppbar.removeOnLayoutChangeListener(listener) }
    }
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
