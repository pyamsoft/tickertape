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

package com.pyamsoft.tickertape.stocks.remote.storage

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

internal abstract class AbstractStorage<T : Any>
protected constructor(
    private val enforcer: ThreadEnforcer,
) : CookieProvider<T> {

  private val mutex = Mutex()

  private var storedCookie = ""
  private var storedToken: T? = null

  // Internally use a cachify so that we have do not make multiple upstream calls at the same time
  private val cookieCache = cachify { getCookie() }
  private val tokenCache = multiCachify<String, T, String> { getToken(it) }

  @CheckResult
  private suspend fun resolveCookie(): String {
    enforcer.assertOffMainThread()

    val s = storedCookie
    if (s.isNotBlank()) {
      return s
    }

    return mutex.withLock {
      if (storedCookie.isBlank()) {
        storedCookie =
            try {
              cookieCache.call()
            } catch (e: Throwable) {
              Timber.e(e, "Error getting cookie")
              ""
            }
      }

      return@withLock storedCookie
    }
  }

  @CheckResult
  private suspend fun resolveToken(): T? {
    enforcer.assertOffMainThread()
    // Fast path
    val s = storedToken
    if (s != null) {
      return s
    }

    // Get cookie from storage
    val c = resolveCookie()
    if (c.isBlank()) {
      return null
    }

    return mutex.withLock {
      if (storedToken == null) {
        storedToken =
            try {
              tokenCache.key(c).call(c)
            } catch (e: Throwable) {
              Timber.e(e, "Error getting token")
              null
            }
      }

      return@withLock storedToken
    }
  }

  @CheckResult
  private suspend fun awaitToken(): T? {
    enforcer.assertOffMainThread()

    var crumb = resolveToken()
    var count = 0

    // Attempt to do this a few times just in case
    while (crumb == null && count < 2) {
      reset()
      crumb = resolveToken()
      ++count
    }

    return crumb
  }

  @CheckResult
  protected suspend inline fun <R : Any> attemptAuthedRequest(block: (T) -> R): R {
    enforcer.assertOffMainThread()

    var token = awaitToken() ?: throw MISSING_COOKIE_EXCEPTION

    var attempt = 0
    while (!validateToken(token)) {
      if (attempt >= 3) {
        Timber.w("We attempted to validate the token but were never able to.")
        throw MISSING_COOKIE_EXCEPTION
      }

      ++attempt
      Timber.w("Token failed to be validated. Claim again")
      reset()
      token = awaitToken() ?: throw MISSING_COOKIE_EXCEPTION
    }

    return block(token)
  }

  protected suspend fun reset() =
      mutex.withLock {
        enforcer.assertOffMainThread()

        storedToken = null
        storedCookie = ""

        cookieCache.clear()
        tokenCache.clear()
      }

  @CheckResult protected abstract suspend fun validateToken(token: T): Boolean

  @CheckResult protected abstract suspend fun getCookie(): String

  @CheckResult protected abstract suspend fun getToken(cookie: String): T

  companion object {
    private val MISSING_COOKIE_EXCEPTION =
        RuntimeException("Unable to authorize device for stock data, please try again")
  }
}
