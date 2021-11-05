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

package com.pyamsoft.tickertape.portfolio.manage.position

import android.os.Bundle
import com.pyamsoft.pydroid.ui.R as R2
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.databinding.LayoutFrameBinding
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.portfolio.manage.PositionManageDialog
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsControllerEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsList
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsViewEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsViewModel
import javax.inject.Inject

internal class PositionsFragment : Fragment(), UiController<PositionsControllerEvent> {

  @JvmField @Inject internal var list: PositionsList? = null

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by viewModels<PositionsViewModel> { factory.requireNotNull().create(this) }

  private var stateSaver: StateSaver? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R2.layout.layout_frame, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    val binding = LayoutFrameBinding.bind(view)
    PositionManageDialog.getInjector(this)
        .plusPositionsComponent()
        .create(viewLifecycleOwner)
        .plusPositionComponent()
        .create(binding.layoutFrame)
        .inject(this)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            list.requireNotNull(),
        ) {
          return@createComponent when (it) {
            is PositionsViewEvent.ForceRefresh -> viewModel.handleFetchPortfolio(true)
            is PositionsViewEvent.Remove -> viewModel.handleRemove(it.index)
          }
        }
  }

  override fun onControllerEvent(event: PositionsControllerEvent) {}

  override fun onStart() {
    super.onStart()
    viewModel.handleFetchPortfolio(false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    list = null
  }

  companion object {

    const val TAG = "PositionsFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return PositionsFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
