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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import javax.inject.Inject

class NotificationInjector @Inject constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: NotificationViewModeler? = null

  override fun onDispose() {
    viewModel = null
  }

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusAlerts().create().inject(this)
  }
}

@Composable
private fun MountHooks(
    viewModel: NotificationViewModeler,
) {
  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
fun NotificationEntry(
    modifier: Modifier = Modifier,
) {
  val component = rememberComposableInjector { NotificationInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  val scope = rememberCoroutineScope()

  MountHooks(
      viewModel = viewModel,
  )

  NotificationScreen(
      modifier = modifier,
      state = viewModel.state,
      onTapeNotificationToggled = { viewModel.handleTapeNotificationToggled(scope = scope) },
      onTapePageSizeChanged = { size ->
        viewModel.handleTapePageSizeChanged(
            scope = scope,
            size = size,
        )
      },
      onBigMoverNotificationToggled = {
        viewModel.handleBigMoverNotificationToggled(scope = scope)
      },
  )
}
