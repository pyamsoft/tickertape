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

package com.pyamsoft.tickertape.main

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.quote.screen.WatchlistDigParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface MainViewState : UiViewState {
  val theme: StateFlow<Theming.Mode>
  val watchlistDigParams: StateFlow<WatchlistDigParams?>
}

@Stable
@ActivityScope
class MutableMainViewState @Inject internal constructor() : MainViewState {
  override val theme = MutableStateFlow(Theming.Mode.SYSTEM)
  override val watchlistDigParams = MutableStateFlow<WatchlistDigParams?>(null)
}
