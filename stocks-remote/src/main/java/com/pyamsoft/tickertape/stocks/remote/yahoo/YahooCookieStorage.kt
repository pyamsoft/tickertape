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

import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.storage.AbstractStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class YahooCookieStorage
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @YahooApi private val service: YahooCookieService,
) :
    AbstractStorage<YahooCrumb>(
        enforcer = enforcer,
    ) {

  override suspend fun getCookie(): String {
    val page = service.getCookie(accept = YF_ACCEPT_STRING)
    val cookies = page.headers().values("Set-Cookie")
    return cookies.joinToString(";")
  }

  override suspend fun getToken(cookie: String): YahooCrumb {
    val newCrumb = service.getCrumb(cookie = cookie)
    return YahooCrumb(
        cookie = cookie,
        crumb = newCrumb,
    )
  }

  override suspend fun <T : Any> withAuth(block: suspend (YahooCrumb) -> T): T =
      withContext(context = Dispatchers.Default) {
        try {
          return@withContext attemptAuthedRequest { block(it) }
        } catch (e: Throwable) {
          if (e is HttpException) {
            if (e.code() == 401) {
              Timber.w("YF returned a 401. We have a bad cookie or crumb.")

              // Clear the cookie and crumb and try again
              reset()

              // Try one more time
              Timber.w("Try again after cookie/crumb reset")
              return@withContext attemptAuthedRequest { block(it) }
            }
          }

          throw e
        }
      }

  companion object {
    // Need to pass this Accept header or YF does not return set-cookies
    private const val YF_ACCEPT_STRING =
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"
  }
}
