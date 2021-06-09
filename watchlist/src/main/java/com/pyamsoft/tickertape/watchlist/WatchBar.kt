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
import com.pyamsoft.tickertape.watchlist.databinding.WatchBarBinding
import javax.inject.Inject

class WatchBar @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<WatchListViewState, WatchListViewEvent, WatchBarBinding>(parent) {

    override val viewBinding = WatchBarBinding::inflate

    override val layoutRoot by boundView { watchbarRoot }

    init {
        doOnInflate {
            binding.watchbarBottomNav.background = null
            binding.watchbarBottomNav.menu.findItem(R.id.menu_placeholder)?.isEnabled = false
        }
    }

}