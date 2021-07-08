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

package com.pyamsoft.tickertape.portfolio

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.applyAppBarOffset
import javax.inject.Inject

class PortfolioHeader
@Inject
internal constructor(parent: ViewGroup, owner: LifecycleOwner, appBarActivity: AppBarActivity) :
    BasePortfolioHeader<PortfolioViewState>(parent) {

  init {
    doOnInflate { binding.portfolioHeaderCollapse.applyAppBarOffset(appBarActivity, owner) }
  }

  override fun onRender(state: UiRender<PortfolioViewState>) {
    handleRender(state)
  }
}
