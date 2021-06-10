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
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.util.doOnApplyWindowInsets
import timber.log.Timber

private inline fun watchToolbarOffset(
    view: View,
    owner: LifecycleOwner,
    crossinline onNewMargin: (Int) -> Unit,
) {
  view.doOnApplyWindowInsets(owner) { _, insets, _ ->
    val toolbarTopMargin = insets.systemWindowInsetTop
    onNewMargin(toolbarTopMargin)
  }
}

private fun applyNewViewOffset(
    view: View,
    initialTopPadding: Int,
    offset: Int?,
) {
  if (offset == null) {
    return
  }

  val newPadding = initialTopPadding + offset
  Timber.d("Apply new offset padding: $view $newPadding")
  view.updatePadding(top = newPadding)
}

fun View.applyToolbarOffset(owner: LifecycleOwner) {
  val initialTopPadding = this.paddingTop

  // Keep track off last seen values here

  watchToolbarOffset(this, owner) { newOffset ->
    applyNewViewOffset(this, initialTopPadding, newOffset)
  }
}
