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

package com.pyamsoft.tickertape.notification

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.UnitControllerEvent
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.requireAppBarActivity
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutCoordinatorBinding
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import javax.inject.Inject

class NotificationFragment : Fragment(), UiController<UnitControllerEvent> {

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<NotificationViewModel>(activity = true) {
    factory?.create(this)
  }

  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var list: NotificationList? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.layout_coordinator, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Animate layout changes
    val binding =
        LayoutCoordinatorBinding.bind(view).apply {
          layoutCoordinator.layoutTransition = LayoutTransition()
        }

    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusNotificationComponent()
        .create(
            requireAppBarActivity(),
            requireToolbarActivity(),
            requireActivity(),
            viewLifecycleOwner)
        .plusNotificationComponent()
        .create(binding.layoutCoordinator)
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
              is NotificationViewEvent.UpdateBigMover -> viewModel.handleBigMoverUpdated(it.enabled)
              is NotificationViewEvent.UpdateTape -> viewModel.handleTapeUpdated(it.enabled)
          }
        }
  }

  override fun onControllerEvent(event: UnitControllerEvent) {
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

    const val TAG = "NotificationFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return NotificationFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
