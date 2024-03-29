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
import com.pyamsoft.tickertape.stocks.remote.network.NetworkTopResponse
import com.pyamsoft.tickertape.stocks.remote.network.NetworkTrendingResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

internal interface TopService {

  @CheckResult
  @GET("https://query1.finance.yahoo.com/v1/finance/trending/US")
  suspend fun getTrending(
      @Header("Cookie") cookie: String,
      @Query("crumb", encoded = true) crumb: String,
      @Query("count") count: Int
  ): NetworkTrendingResponse

  @CheckResult
  @GET("https://query2.finance.yahoo.com/v1/finance/screener/predefined/saved?$DEFAULT_OPTIONS")
  suspend fun getScreener(
      @Header("Cookie") cookie: String,
      @Query("crumb", encoded = true) crumb: String,
      @Query("count") count: Int,
      @Query("scrIds") screener: String,
  ): NetworkTopResponse

  companion object {
    private const val DEFAULT_OPTIONS = "formatted=false&lang=en-US&region=US&"
  }
}
