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

package com.pyamsoft.tickertape.core

import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.pyamsoft.pydroid.arch.SavedStateViewModelFactory
import com.pyamsoft.pydroid.arch.UiSavedState
import dagger.Reusable
import javax.inject.Inject
import javax.inject.Provider

interface TickerViewModelFactory {

  @CheckResult fun create(owner: SavedStateRegistryOwner): ViewModelProvider.Factory
}

@Reusable
internal class TickerViewModelFactoryImpl
@Inject
internal constructor(
    private val viewModels: Map<Class<*>, @JvmSuppressWildcards Provider<ViewModel>>,
) : TickerViewModelFactory {

  override fun create(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
    return object : SavedStateViewModelFactory(owner, defaultArgs = null) {

      override fun <T : ViewModel> createViewModel(
          modelClass: Class<T>,
          savedState: UiSavedState
      ): ViewModel {
        return viewModels[modelClass]?.get() ?: fail(modelClass)
      }
    }
  }
}
