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

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputEditText
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.ui.UiEditTextDelegate
import timber.log.Timber

abstract class BasePositionsEditable<B : ViewBinding> protected constructor(parent: ViewGroup) :
    BaseUiView<PositionsAddViewState, PositionsAddViewEvent, B>(parent) {

  private var delegate: UiEditTextDelegate? = null

  init {
    doOnTeardown {
      delegate?.handleTeardown()
      delegate = null
    }

    doOnInflate {
      val editText = provideEditText()

      delegate =
          UiEditTextDelegate.create(editText) { numberString ->
        // Blank string reset to 0
        if (numberString.isBlank()) {
          publish(PositionsAddViewEvent.UpdateSharePrice(StockMoneyValue.none()))
          return@create true
        }

        val value = numberString.toDoubleOrNull()
        if (value == null) {
          Timber.w("Invalid sharePrice $numberString")
          return@create false
        }

        publish(provideEvent(value))
        return@create true
      }
          .apply { handleCreate() }
    }
  }

  @CheckResult protected abstract fun provideEditText(): TextInputEditText

  @CheckResult protected abstract fun provideEvent(value: Double): PositionsAddViewEvent

  protected fun handleValueChanged(text: String) {
    requireNotNull(delegate).handleTextChanged(text)
  }
}
