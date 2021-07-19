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

package com.pyamsoft.tickertape.portfolio.manage.position.add

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsDateModule
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [PositionsDateModule::class])
internal interface BasePositionsAddComponent {

  fun plusAddComponent(): PositionsAddComponent.Factory

  fun plusDateComponent(): PositionsDateComponent.Factory

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(): BasePositionsAddComponent
  }
}
