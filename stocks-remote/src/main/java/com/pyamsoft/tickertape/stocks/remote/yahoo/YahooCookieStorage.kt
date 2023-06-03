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
import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class YahooCookieStorage
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    @YahooApi service: YahooCookieService,
) : YahooCrumbProvider {

  private val mutex = Mutex()

  private var storedCookie = ""
  private var storedCrumb: YahooCrumb? = null

  // Internally use a cachify so that we have do not make multiple upstream calls at the same time
  private val cookieCache = cachify { service.getCookie(accept = YF_ACCEPT_STRING) }
  private val crumbCache = multiCachify<String, String, String> { service.getCrumb(cookie = it) }

  @CheckResult
  private suspend fun getCookie(): String {
    enforcer.assertOffMainThread()

    val s = storedCookie
    if (s.isNotBlank()) {
      return s
    }

    return mutex.withLock {
      if (storedCookie.isBlank()) {
        storedCookie =
            try {
              val page = cookieCache.call()
              val cookies = page.headers().values("Set-Cookie")
              cookies.joinToString(";")
            } catch (e: Throwable) {
              Timber.e(e, "Error getting YF cookie")
              ""
            }
      }

      return@withLock storedCookie
    }
  }

  @CheckResult
  private suspend fun getCrumb(): YahooCrumb? {
    enforcer.assertOffMainThread()
    // Fast path
    val s = storedCrumb
    if (s != null) {
      return s
    }

    // Get cookie from storage
    val c = getCookie()
    Timber.d("Got cookie from YF: $c")
    if (c.isBlank()) {
      return null
    }

    return mutex.withLock {
      if (storedCrumb == null) {
        storedCrumb =
            try {
              val newCrumb = crumbCache.key(c).call(c)
              YahooCrumb(
                  cookie = c,
                  crumb = newCrumb,
              )
            } catch (e: Throwable) {
              Timber.e(e, "Error getting YF crumb")
              null
            }
      }

      return@withLock storedCrumb
    }
  }

  @CheckResult
  private suspend fun resolveCrumb(): YahooCrumb? {
    enforcer.assertOffMainThread()

    var crumb = getCrumb()
    var count = 0

    // Attempt to do this a few times just in case
    while (crumb == null && count < 2) {
      clearStored()
      crumb = getCrumb()
      ++count
    }

    return crumb
  }

  @CheckResult
  private suspend inline fun <T : Any> attemptAuthedRequest(block: (YahooCrumb) -> T): T {
    enforcer.assertOffMainThread()

    val crumb = resolveCrumb() ?: throw MISSING_COOKIE_EXCEPTION
    return block(crumb)
  }

  private suspend fun clearStored() =
      mutex.withLock {
        enforcer.assertOffMainThread()

        storedCrumb = null
        storedCookie = ""

        cookieCache.clear()
        crumbCache.clear()
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
              clearStored()

              // Try one more time
              Timber.w("Try again after cookie/crumb reset")
              return@withContext attemptAuthedRequest { block(it) }
            }
          }

          throw e
        }
      }

  companion object {
    private val MISSING_COOKIE_EXCEPTION =
        RuntimeException("Unable to authorize device for stock data, please try again")

    // Need to pass this Accept header or YF does not return set-cookies
    private const val YF_ACCEPT_STRING =
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"
  }
}
