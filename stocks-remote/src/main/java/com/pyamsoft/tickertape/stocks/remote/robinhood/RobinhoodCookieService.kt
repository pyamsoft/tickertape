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

package com.pyamsoft.tickertape.stocks.remote.robinhood

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.remote.converter.ScalarResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

internal interface RobinhoodCookieService {

  /** Get the Cookie from RH (we need to ask for some stock, just use AAPL) */
  @CheckResult
  @ScalarResponse
  @GET("https://robinhood.com/stocks/AAPL")
  suspend fun getCookie(@Header("Accept") accept: String): Response<String>

  /** Trade cookie for a token */
  @CheckResult
  @GET("https://robinhood.com/api/public/get_token")
  suspend fun getToken(@Header("Cookie") cookie: String): RobinhoodTokenResponse
}
