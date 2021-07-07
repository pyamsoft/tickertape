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

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.core.os.bundleOf
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.tickertape.quote.ui.QuoteViewState

abstract class QuoteBaseController protected constructor() : QuoteDelegateView {

  fun create(savedInstanceState: UiSavedStateReader) {
    onCreate(savedInstanceState)
  }

  protected open fun onCreate(savedInstanceState: UiSavedStateReader) {}

  fun saveState(outState: UiSavedStateWriter) {
    onSaveState(outState)
  }

  protected open fun onSaveState(outState: UiSavedStateWriter) {}

  fun destroy() {
    onDestroy()
  }

  protected open fun onDestroy() {}

  fun handleUpdateState(state: QuoteViewState) {
    onStateUpdated(state)
  }

  protected abstract fun onStateUpdated(state: QuoteViewState)

  companion object {

    @JvmStatic
    @CheckResult
    protected fun UiSavedStateReader.toBundle(): Bundle {
      val everything = this.all()
      val data = everything.entries.map { Pair(it.key, it.key) }.toTypedArray()
      return bundleOf(*data)
    }
  }
}
