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

package com.pyamsoft.tickertape.stocks.yahoo.service

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.yahoo.network.NetworkKeyStatisticsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface KeyStatisticsService {

  @CheckResult
  @GET("https://query2.finance.yahoo.com/v10/finance/quoteSummary/{symbol}")
  suspend fun getStatistics(
      @Path("symbol") symbol: String,
      @Query("modules") modules: String
  ): NetworkKeyStatisticsResponse
}
