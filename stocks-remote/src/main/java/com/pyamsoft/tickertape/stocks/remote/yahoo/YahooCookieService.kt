/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.stocks.remote.yahoo

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.remote.converter.ScalarResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

internal interface YahooCookieService {

  /**
   * Get a cookie
   */
  @CheckResult
  @ScalarResponse
  @GET("https://finance.yahoo.com/quote/AAPL")
  suspend fun getCookie(@Header("Accept") accept: String): Response<String>

  /**
   * Trade cookie for a token
   */
  @CheckResult
  @ScalarResponse
  @GET("https://query1.finance.yahoo.com/v1/test/getcrumb")
  suspend fun getCrumb(@Header("Cookie") cookie: String): String
}
