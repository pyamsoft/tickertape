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

object ViewFixes {

  @CheckResult
  inline fun correctMultiWindow(view: View, crossinline correction: (View) -> Unit): Unregister {
    view.apply {
      val listener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ -> correction(v) }
      addOnLayoutChangeListener(listener)
      return Unregister { removeOnLayoutChangeListener(listener) }
    }
  }

  // A view can sometimes size weirdly when in multi-window
  // if the Activity is opened, and then Home is pushed and then the app is
  // opened again from the Launcher
  @CheckResult
  fun correctMultiWindowMargin(view: View): Unregister {
    val initialLeft = view.marginLeft
    val initialTop = view.marginTop
    val initialRight = view.marginRight
    val initialBottom = view.marginBottom
    return correctMultiWindow(view) {
      it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.bottomMargin = initialBottom
        this.topMargin = initialTop
        this.leftMargin = initialLeft
        this.rightMargin = initialRight
      }
    }
  }

  // For some views nested inside of other UiView components, the match_parent does not fill the
  // parent
  // fully.
  fun correctMatchParentHeight(view: View, parent: View) {
    parent.post { view.post { view.updateLayoutParams { this.height = parent.height } } }
  }
}

fun interface Unregister {

  fun unregister()
}
