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

import android.app.Activity
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.tickertape.portfolio.item.PortfolioItemComponent
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
internal interface BasePortfolioComponent {

  @CheckResult fun plusPortfolioComponent(): PortfolioComponent.Factory

  /** Not actually used, just here so graph can compile */
  @CheckResult
  @Suppress("FunctionName")
  fun `$$daggerRequiredPortfolioItemComponent`(): PortfolioItemComponent.Factory

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance toolbarActivity: ToolbarActivity,
        @BindsInstance appBarActivity: AppBarActivity,
        @BindsInstance activity: Activity,
        @BindsInstance owner: LifecycleOwner,
    ): BasePortfolioComponent
  }
}
