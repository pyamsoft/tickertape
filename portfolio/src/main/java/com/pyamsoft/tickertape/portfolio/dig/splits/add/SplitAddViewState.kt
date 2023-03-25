/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface SplitAddViewState : UiViewState {
  val datePicker: StateFlow<LocalDate?>
  val isSubmitting: StateFlow<Boolean>
  val isSubmittable: StateFlow<Boolean>
  val preSplitShareCount: StateFlow<String>
  val postSplitShareCount: StateFlow<String>
  val splitDate: StateFlow<LocalDate?>
}

@Stable
class MutableSplitAddViewState @Inject internal constructor() : SplitAddViewState {
  override val datePicker = MutableStateFlow<LocalDate?>(null)
  override val isSubmitting = MutableStateFlow(false)
  override val isSubmittable = MutableStateFlow(false)
  override val preSplitShareCount = MutableStateFlow("")
  override val postSplitShareCount = MutableStateFlow("")
  override val splitDate = MutableStateFlow<LocalDate?>(null)
}
