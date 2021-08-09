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

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

object ViewFixes {

  // Watch a view and run function when the layout of said view changes (for, we assume, MW reasons)
  @CheckResult
  inline fun listenLayoutChanged(view: View, crossinline correction: (View) -> Unit): Unregister {
    val listener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ -> correction(v) }
    view.addOnLayoutChangeListener(listener)
    return Unregister { view.removeOnLayoutChangeListener(listener) }
  }

  @CheckResult
  private inline fun correctMatchParent(
      view: View,
      parent: View,
      crossinline onUpdate: ViewGroup.LayoutParams.(parent: View) -> Unit
  ): Unregister {
    return listenLayoutChanged(parent) { p ->
      view.updateLayoutParams { this.onUpdate(p) }
    }
  }

  // For some views nested inside of other UiView components, the match_parent does not fill the
  // parent fully.
  @CheckResult
  fun correctMatchParentHeight(view: View, parent: View): Unregister {
    return correctMatchParent(view, parent) { this.height = it.height }
  }

  // For some views nested inside of other UiView components, the match_parent does not fill the
  // parent fully.
  @CheckResult
  fun correctMatchParentWidth(view: View, parent: View): Unregister {
    return correctMatchParent(view, parent) { this.width = it.width }
  }

  // Captures the initial padding for a given view and provides a function which, when called, will
  // reset the padding of the view back to its initial state
  @CheckResult
  fun captureAndResetInitialPadding(view: View): Reset {
    val initialLeft = view.paddingLeft
    val initialTop = view.paddingTop
    val initialRight = view.paddingRight
    val initialBottom = view.paddingBottom
    return Reset {
      view.updatePadding(
          left = initialLeft,
          top = initialTop,
          right = initialRight,
          bottom = initialBottom,
      )
    }
  }

  // Captures the initial padding for a given view and provides a function which, when called, will
  // reset the padding of the view back to its initial state
  @CheckResult
  fun captureAndResetInitialMargin(view: View): Reset {
    val initialLeft = view.marginLeft
    val initialTop = view.marginTop
    val initialRight = view.marginRight
    val initialBottom = view.marginBottom

    return Reset {
      view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.leftMargin = initialLeft
        this.rightMargin = initialRight
        this.topMargin = initialTop
        this.bottomMargin = initialBottom
      }
    }
  }
}

fun interface Unregister {

  fun unregister()
}

fun interface Reset {

  fun reset()
}
