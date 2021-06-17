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

package com.pyamsoft.tickertape.portfolio.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutFrameBinding
import javax.inject.Inject
import timber.log.Timber

internal class PositionsFragment : Fragment(), UiController<HoldingControllerEvent> {

  @JvmField @Inject internal var list: PositionsList? = null

  @JvmField @Inject internal var factory: HoldingViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<HoldingViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  private var stateSaver: StateSaver? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_frame, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    val binding = LayoutFrameBinding.bind(view)
    PositionManageDialog.getInjector(this)
        .plusPositionsComponent()
        .create(this, viewLifecycleOwner, binding.layoutFrame)
        .inject(this)

    val list = requireNotNull(list)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            list,
        ) {
          return@createComponent when (it) {
            is HoldingViewEvent.ForceRefresh -> viewModel.handleFetchPortfolio(true)
            is HoldingViewEvent.Remove -> viewModel.handleRemove(it.index)
            is HoldingViewEvent.UpdateNumberOfShares ->
                viewModel.handleUpdateNumberOfShares(it.number)
            is HoldingViewEvent.UpdateSharePrice -> viewModel.handleUpdateSharePrice(it.price)
            is HoldingViewEvent.Commit -> viewModel.handleCreatePosition()
            is HoldingViewEvent.ListPositions -> Timber.d("ASDASD")
          }
        }
  }

  override fun onControllerEvent(event: HoldingControllerEvent) {}

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
