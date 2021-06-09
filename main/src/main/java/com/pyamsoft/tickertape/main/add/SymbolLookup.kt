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

package com.pyamsoft.tickertape.main.add

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.main.databinding.SymbolAddLookupBinding
import javax.inject.Inject

class SymbolLookup @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<SymbolAddViewState, SymbolAddViewEvent, SymbolAddLookupBinding>(parent) {

  override val viewBinding = SymbolAddLookupBinding::inflate

  override val layoutRoot by boundView { symbolAddLookupRoot }

  // NOTE(Peter): Hack because Android does not allow us to use Controlled view components like
  // React does by binding input and drawing to the render loop.
  //
  // This initialRenderPerformed variable allows us to set the initial state of a view once, and
  // bind listeners to
  // it because the state.item is only available in render instead of inflate. Once the firstRender
  // has set the view component up, the actual input will no longer be tracked via state render
  // events,
  // so the input is uncontrolled.
  private var initialRenderPerformed = false

  private var textWatcher: TextWatcher? = null

  init {
    doOnTeardown {
      killWatcher()
      binding.symbolAddLookupEdit.text?.clear()
    }

    doOnInflate {
      killWatcher()
      val watcher = createTextWatcher()
      binding.symbolAddLookupEdit.addTextChangedListener(watcher)
      textWatcher = watcher
    }
  }

  private fun killWatcher() {
    textWatcher?.also { binding.symbolAddLookupEdit.removeTextChangedListener(it) }
    textWatcher = null
  }

  @CheckResult
  private fun createTextWatcher(): TextWatcher {
    return object : TextWatcher {

      private var oldText = binding.symbolAddLookupEdit.text?.toString().orEmpty()

      override fun afterTextChanged(s: Editable) {
        val newText = s.toString()
        val previousText = oldText
        if (newText != previousText) {
          oldText = newText
          publish(SymbolAddViewEvent.UpdateSymbol(newText))
        }
      }

      override fun beforeTextChanged(
          s: CharSequence,
          start: Int,
          count: Int,
          after: Int,
      ) {}

      override fun onTextChanged(
          s: CharSequence,
          start: Int,
          before: Int,
          count: Int,
      ) {}
    }
  }

  override fun onRender(state: UiRender<SymbolAddViewState>) {
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbolChanged(it) }
  }

  private fun handleSymbolChanged(symbol: String) {
    if (initialRenderPerformed) {
      return
    }

    initialRenderPerformed = true
    if (symbol.isNotBlank()) {
      ignoreWatcher { binding.symbolAddLookupEdit.setTextKeepState(symbol) }
    }
  }

  private inline fun ignoreWatcher(block: () -> Unit) {
    textWatcher?.also { binding.symbolAddLookupEdit.removeTextChangedListener(it) }
    block()
    textWatcher?.also { binding.symbolAddLookupEdit.addTextChangedListener(it) }
  }
}
