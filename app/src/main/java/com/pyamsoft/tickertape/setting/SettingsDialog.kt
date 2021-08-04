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

package com.pyamsoft.tickertape.setting

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
import com.pyamsoft.pydroid.arch.UnitControllerEvent
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.app.makeFullscreen
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.commitNow
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import javax.inject.Inject

internal class SettingsDialog : AppCompatDialogFragment(), UiController<UnitControllerEvent> {

  private var stateSaver: StateSaver? = null

  @JvmField @Inject
  internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<SettingsViewModel>(activity = true) {
    factory?.create(requireActivity())
  }

  @JvmField @Inject
  internal var toolbar: SettingToolbar? = null

  @JvmField @Inject
  internal var container: SettingsContainer? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    makeFullscreen()

    val binding = LayoutConstraintBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(view.context)
      .plusSettingsComponent()
      .create(binding.layoutConstraint)
      .inject(this)

    val toolbar = toolbar.requireNotNull()
    val container = container.requireNotNull()

    stateSaver = createComponent(savedInstanceState, viewLifecycleOwner, viewModel, this, toolbar, container,) {
      return@createComponent when(it) {
        is SettingsViewEvent.Close -> dismiss()
      }
    }

    binding.layoutConstraint.layout {
        toolbar.also {
          connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
          connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
          connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
          constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        }

        container.also {
          connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
          connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
          connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
          connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
          constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
          constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }

    // Add fragment for settings
    childFragmentManager.commitNow(viewLifecycleOwner) {
      replace(container.id(), SettingsFragment.newInstance(), SettingsFragment.TAG)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    factory = null
    stateSaver = null

    toolbar = null
    container = null
  }

  override fun onControllerEvent(event: UnitControllerEvent) {
  }

  companion object {

    const val TAG = "SettingsDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(): DialogFragment {
      return SettingsDialog().apply {
        arguments = Bundle().apply {  }
      }
    }
  }
}
