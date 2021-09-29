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

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import timber.log.Timber

class UiEditTextDelegate
private constructor(
    private var editText: EditText?,
    private var onTextChanged: ((String) -> Boolean)?
) {

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

  @CheckResult
  private fun getEditText(): EditText {
    return editText.requireNotNull()
  }

  private fun killWatcher() {
    textWatcher?.also { editText?.removeTextChangedListener(it) }
    textWatcher = null
  }

  private inline fun ignoreWatcher(block: (EditText) -> Unit) {
    textWatcher?.also { watcher ->
      val edit = getEditText()
      ignoreTextWatcher(edit, watcher, block)
    }
  }

  fun handleCreate() {
    killWatcher()
    val edit = getEditText()
    val watcher = createTextWatcher(edit, onTextChanged.requireNotNull())
    edit.addTextChangedListener(watcher)
    textWatcher = watcher
  }

  fun handleTeardown() {
    killWatcher()
    editText?.text?.clear()

    editText = null
    onTextChanged = null
  }

  private fun applyText(text: String, force: Boolean) {
    // Don't keep setting text here as it is too slow
    val skipRender = if (force) false else initialRenderPerformed
    if (skipRender) {
      return
    }
    initialRenderPerformed = true

    ignoreWatcher { edit ->
      if (text.isNotBlank()) {
        edit.setTextKeepState(text)
      } else {
        edit.text.clear()
      }
    }
  }

  fun handleTextChanged(data: Data) {
    applyText(data.text, data.force)
  }

  companion object {

    /**
     * onTextChanged is a function which given a string returns a boolean. If it returns false then
     * the text change is rolled back to the previous text If it returns true, the text change is
     * approved and continues
     */
    @JvmStatic
    @CheckResult
    fun create(editText: EditText, onTextChanged: (String) -> Boolean): UiEditTextDelegate {
      return UiEditTextDelegate(editText, onTextChanged)
    }

    @JvmStatic
    private inline fun ignoreTextWatcher(
        editText: EditText,
        textWatcher: TextWatcher,
        block: (EditText) -> Unit
    ) {
      editText.removeTextChangedListener(textWatcher)
      block(editText)
      editText.addTextChangedListener(textWatcher)
    }

    @JvmStatic
    @CheckResult
    private inline fun createTextWatcher(
        editText: EditText,
        crossinline onChange: (String) -> Boolean
    ): TextWatcher {
      return object : TextWatcher {

        private var oldText = editText.text?.toString().orEmpty()

        override fun afterTextChanged(s: Editable) {
          val newText = s.toString()
          val previousText = oldText
          if (newText != previousText) {
            val approved = onChange(newText)
            if (!approved) {
              Timber.w("Text change was not approved, roll back: $newText")
              ignoreTextWatcher(editText, this) { it.setTextKeepState(previousText) }
            } else {
              oldText = newText
            }
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
  }

  data class Data
  internal constructor(
      val text: String,
      val force: Boolean,
  ) {

    companion object {

      @JvmField val EMPTY = Data(text = "", force = true)
    }
  }
}

@CheckResult
fun String.asEditData(force: Boolean = false): UiEditTextDelegate.Data {
  return if (this.isBlank()) UiEditTextDelegate.Data.EMPTY
  else UiEditTextDelegate.Data(text = this, force = force)
}
