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

package com.pyamsoft.tickertape.watchlist.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.app.makeFullWidth
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.watchlist.add.SymbolAddCommit
import com.pyamsoft.tickertape.watchlist.add.SymbolAddControllerEvent
import com.pyamsoft.tickertape.watchlist.add.SymbolAddViewEvent
import com.pyamsoft.tickertape.watchlist.add.SymbolAddViewModel
import com.pyamsoft.tickertape.watchlist.add.SymbolAddViewState
import com.pyamsoft.tickertape.watchlist.add.SymbolLookup
import com.pyamsoft.tickertape.watchlist.add.SymbolToolbar
import javax.inject.Inject

internal class SymbolAddDialog : AppCompatDialogFragment(), UiController<SymbolAddControllerEvent> {

  @JvmField @Inject internal var lookup: SymbolLookup? = null

  @JvmField @Inject internal var commit: SymbolAddCommit? = null

  @JvmField @Inject internal var toolbar: SymbolToolbar? = null

  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var factory: SymbolAddViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<SymbolAddViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullWidth()

    val binding = LayoutConstraintBinding.bind(view)

    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusSymbolAddComponent()
        .create(this, requireActivity(), viewLifecycleOwner, binding.layoutConstraint)
        .inject(this)

    val lookup = requireNotNull(lookup)
    val commit = requireNotNull(commit)
    val toolbar = requireNotNull(toolbar)
    val shadow =
        DropshadowView.createTyped<SymbolAddViewState, SymbolAddViewEvent>(binding.layoutConstraint)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            lookup,
            commit,
            toolbar,
            shadow) {
          return@createComponent when (it) {
            is SymbolAddViewEvent.Close -> dismiss()
            is SymbolAddViewEvent.UpdateSymbol -> viewModel.handleLookupSymbol(it.symbol)
            is SymbolAddViewEvent.CommitSymbol -> viewModel.handleCommitSymbol()
          }
        }

    binding.layoutConstraint.layout {
      toolbar.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      shadow.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      lookup.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      commit.also {
        connect(it.id(), ConstraintSet.TOP, lookup.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }
    }
  }

  override fun onControllerEvent(event: SymbolAddControllerEvent) {
    return when (event) {
      is SymbolAddControllerEvent.Close -> dismiss()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    stateSaver?.saveState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()

    factory = null
    stateSaver = null

    commit = null
    lookup = null
    toolbar = null
  }

  companion object {

    const val TAG = "SymbolAddDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(): DialogFragment {
      return SymbolAddDialog().apply { arguments = Bundle().apply {} }
    }
  }
}
