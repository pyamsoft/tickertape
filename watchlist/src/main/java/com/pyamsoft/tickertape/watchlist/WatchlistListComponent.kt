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

package com.pyamsoft.tickertape.watchlist

import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.watchlist.item.WatchlistViewHolder
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface WatchlistListComponent {

  fun inject(holder: WatchlistViewHolder)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(@BindsInstance parent: ViewGroup): WatchlistListComponent
  }
}