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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.tickertape.watchlist.databinding.WatchAddBinding
import javax.inject.Inject

class WatchAdd @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<WatchListViewState, WatchListViewEvent, WatchAddBinding>(parent) {

    override val viewBinding = WatchAddBinding::inflate

    override val layoutRoot by boundView { watchbarAdd }

}