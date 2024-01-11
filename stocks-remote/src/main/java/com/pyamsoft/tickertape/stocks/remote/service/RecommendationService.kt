/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.stocks.remote.service

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.remote.network.NetworkRecommendationResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

internal interface RecommendationService {

  @CheckResult
  @GET("https://query2.finance.yahoo.com/v6/finance/recommendationsbysymbol/{symbol}")
  suspend fun getRecommendations(
      @Header("Cookie") cookie: String,
      @Path("symbol") symbol: String,
      @Query("crumb", encoded = true) crumb: String,
  ): NetworkRecommendationResponse
}
