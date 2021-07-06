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

package com.pyamsoft.tickertape.quote.ui

import android.view.View
import androidx.annotation.CheckResult
import androidx.viewbinding.ViewBinding
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import kotlinx.coroutines.CoroutineScope

abstract class QuoteDelegate<S : UiViewState, V : UiViewEvent, B : ViewBinding>
protected constructor() {

  protected abstract val binding: B

  @CheckResult
  fun id(): Int {
    return root().id
  }

  fun inflate(onViewEvent: (V) -> Unit) {
    onInflate(onViewEvent)
  }

  fun teardown() {
    onTeardown()
  }

  @CheckResult abstract fun root(): View

  abstract fun render(scope: CoroutineScope, state: UiRender<S>)

  protected abstract fun onInflate(onViewEvent: (V) -> Unit)

  protected abstract fun onTeardown()
}
