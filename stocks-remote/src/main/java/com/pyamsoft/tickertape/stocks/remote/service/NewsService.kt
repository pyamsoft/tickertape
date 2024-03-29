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
import com.pyamsoft.tickertape.stocks.remote.network.NetworkNewsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

internal interface NewsService {

  @CheckResult
  @GET("https://api.robinhood.com/midlands/news/")
  suspend fun getNews(
      @Query("symbol") symbol: String,
      @Header("Authorization") token: String,
      @Header("User-Agent") userAgent: String,
  ): NetworkNewsResponse
}
