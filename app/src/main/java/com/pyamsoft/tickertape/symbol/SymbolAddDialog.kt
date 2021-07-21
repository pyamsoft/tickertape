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

package com.pyamsoft.tickertape.symbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.app.makeFullWidth
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.tickertape.main.add.SymbolAddControllerEvent
import com.pyamsoft.tickertape.main.add.SymbolAddHoldingType
import com.pyamsoft.tickertape.main.add.SymbolAddViewEvent
import com.pyamsoft.tickertape.main.add.SymbolAddViewModel
import com.pyamsoft.tickertape.main.add.SymbolAddViewState
import com.pyamsoft.tickertape.main.add.SymbolLookup
import com.pyamsoft.tickertape.main.add.SymbolResultList
import com.pyamsoft.tickertape.main.add.SymbolToolbar
import dagger.Lazy
import javax.inject.Inject

internal abstract class SymbolAddDialog<V : SymbolAddViewModel> :
    AppCompatDialogFragment(), UiController<SymbolAddControllerEvent> {

  @JvmField @Inject internal var lookup: SymbolLookup? = null

  @JvmField @Inject internal var toolbar: SymbolToolbar? = null

  @JvmField @Inject internal var list: SymbolResultList? = null

  @JvmField @Inject internal var typeProvider: Lazy<SymbolAddHoldingType>? = null

  private var stateSaver: StateSaver? = null

  protected abstract val viewModel: V

  protected abstract val isHoldingTypeSupported: Boolean

  final override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  final override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullWidth()

    val binding = LayoutConstraintBinding.bind(view)
    onInject(binding.layoutConstraint, savedInstanceState)

    val list = requireNotNull(list)
    val lookup = requireNotNull(lookup)
    val toolbar = requireNotNull(toolbar)
    val typeProvider = typeProvider.requireNotNull()
    val shadow =
        DropshadowView.createTyped<SymbolAddViewState, SymbolAddViewEvent>(binding.layoutConstraint)

    val views =
        mutableListOf(
            lookup,
            list,
            toolbar,
            shadow,
        )

    val typeView: SymbolAddHoldingType? =
        if (isHoldingTypeSupported) {
          typeProvider.get().requireNotNull().also { views.add(it) }
        } else {
          null
        }

    stateSaver =
        createComponent(
            savedInstanceState, viewLifecycleOwner, viewModel, this, *views.toTypedArray()) {
          return@createComponent when (it) {
            is SymbolAddViewEvent.Close -> dismiss()
            is SymbolAddViewEvent.CommitSymbol -> viewModel.handleCommitSymbol()
            is SymbolAddViewEvent.UpdateSymbol ->
                viewModel.handleLookupSymbol(it.symbol, isHoldingTypeSupported)
            is SymbolAddViewEvent.SelectResult -> viewModel.handleResultSelected(it.index)
            is SymbolAddViewEvent.UpdateType -> viewModel.handleUpdateType()
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

      typeView?.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.WRAP_CONTENT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      lookup.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        if (typeView != null) {
          connect(it.id(), ConstraintSet.END, typeView.id(), ConstraintSet.START)
        } else {
          connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      list.also {
        connect(it.id(), ConstraintSet.TOP, lookup.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }
  }

  protected abstract fun onInject(view: ViewGroup, savedInstanceState: Bundle?)

  protected abstract fun onTeardown()

  final override fun onControllerEvent(event: SymbolAddControllerEvent) {
    return when (event) {
      is SymbolAddControllerEvent.Close -> dismiss()
    }
  }

  final override fun onSaveInstanceState(outState: Bundle) {
    stateSaver?.saveState(outState)
    super.onSaveInstanceState(outState)
  }

  final override fun onDestroyView() {
    super.onDestroyView()

    stateSaver = null

    lookup = null
    toolbar = null
    list = null
    typeProvider = null

    onTeardown()
  }
}
