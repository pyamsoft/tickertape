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

package com.pyamsoft.tickertape.stocks.okhttp

import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.pyamsoft.pydroid.core.ThreadEnforcer
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpClientLazyCallFactory(
    debug: Boolean,
    private val enforcer: ThreadEnforcer,
) : Call.Factory {

  private val client by lazy {
    enforcer.assertOffMainThread()

    return@lazy OkHttpClient.Builder()
        .socketFactory(DelegatingSocketFactory.create())
        .run {
          var self = this

          if (debug) {
            val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            self = self.addInterceptor(logger)
          }

          return@run self
        }
        .build()
  }

  override fun newCall(request: Request): Call {
    enforcer.assertOffMainThread()
    return client.newCall(request)
  }
}
