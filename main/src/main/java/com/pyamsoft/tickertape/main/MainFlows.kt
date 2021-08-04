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

import androidx.annotation.StringRes
import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.HoldingType

data class MainViewState
internal constructor(
    @StringRes val appNameRes: Int,
    val adding: Boolean,
    val page: MainPage,
    val bottomBarHeight: Int,
) : UiViewState

sealed class MainViewEvent : UiViewEvent {

  object OpenSettings : MainViewEvent()

  object OpenHome : MainViewEvent()

  object OpenWatchList : MainViewEvent()

  object OpenPortfolio : MainViewEvent()

  object OpenNotifications : MainViewEvent()

  data class BottomBarMeasured internal constructor(val height: Int) : MainViewEvent()

  data class OpenAdd internal constructor(val type: HoldingType) : MainViewEvent()

  object AddRequest : MainViewEvent()
}

sealed class MainControllerEvent : UiControllerEvent {

  data class PushPage
  internal constructor(val newPage: MainPage, val oldPage: MainPage?, val force: Boolean) :
      MainControllerEvent()
}
