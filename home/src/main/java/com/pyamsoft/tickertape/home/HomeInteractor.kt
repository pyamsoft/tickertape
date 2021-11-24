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

package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.Ticker

internal interface HomeInteractor {

  @CheckResult
  suspend fun getDayGainers(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>>

  @CheckResult
  suspend fun getDayLosers(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>>

  @CheckResult
  suspend fun getDayShorted(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>>

  @CheckResult
  suspend fun getDayTrending(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>>
}
