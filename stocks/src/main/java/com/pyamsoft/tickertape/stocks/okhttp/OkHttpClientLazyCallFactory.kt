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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.pyamsoft.pydroid.core.Enforcer
import javax.net.SocketFactory
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpClientLazyCallFactory(
    debug: Boolean,
) : Call.Factory {

  private val client by lazy { createOkHttpClient(debug, DelegatingSocketFactory.create()) }

  override fun newCall(request: Request): Call {
    Enforcer.assertOffMainThread()
    return client.newCall(request)
  }

  companion object {

    @JvmStatic
    @CheckResult
    internal fun createOkHttpClient(
        debug: Boolean,
        socketFactory: SocketFactory,
    ): OkHttpClient {
      Enforcer.assertOffMainThread()

      return OkHttpClient.Builder()
          .socketFactory(socketFactory)
          .apply {
            if (debug) {
              addInterceptor(
                  HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            }
          }
          .build()
    }
  }
}
