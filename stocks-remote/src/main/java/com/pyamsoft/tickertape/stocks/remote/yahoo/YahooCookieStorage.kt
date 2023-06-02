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
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
internal class YahooCookieStorage
@Inject
internal constructor(
    private val preferences: YahooCookiePreferences,
    @YahooApi private val service: YahooCookieService,
) : YahooCrumbProvider, YahooCookieManager {

  private val mutex = Mutex()
  private var storedCrumb: YahooCrumb? = null

  private val scope by lazy {
    CoroutineScope(
        context = SupervisorJob() + Dispatchers.Default + CoroutineName(this::class.java.name),
    )
  }

  @OptIn(FlowPreview::class)
  private val cookieFromStorage =
      preferences
          .listenForYahooCookie()
          // If after 1 second we haven't gotten a cookie, we throw an error, and then
          // immediately catch the error and emit an empty string
          .timeout(1.seconds)
          .catch {
            // If the error is from a timeout, catch it and emit empty string, else its
            // an error
            if (it is TimeoutCancellationException) emit("") else throw it
          }
          // Every time we get a new cookie, clear the crumb
          .onEach { clearStoredCrumb() }

  @CheckResult
  private suspend fun getCrumb(): YahooCrumb? =
      withContext(context = Dispatchers.Default) {
        // Fast path
        val s = storedCrumb
        if (s != null) {
          return@withContext s
        }

        // Get cookie from storage
        val c = cookieFromStorage.first()
        if (c.isBlank()) {
          return@withContext null
        }

        return@withContext mutex.withLock {
          if (storedCrumb == null) {
            storedCrumb =
                try {
                  val newCrumb = service.getCrumb(cookie = c)
                  YahooCrumb(
                      cookie = c,
                      crumb = newCrumb.crumb,
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
    var crumb = getCrumb()
    var count = 0

    // Attempt to do this a few times just in case
    while (crumb == null && count < 2) {
      reset()
      crumb = getCrumb()
      ++count
    }

    return crumb
  }

  @CheckResult
  private suspend inline fun <T : Any> attemptAuthedRequest(block: (YahooCrumb) -> T): T {
    val crumb = resolveCrumb() ?: throw MISSING_COOKIE_EXCEPTION
    return block(crumb)
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
              preferences.removeYahooCookie()
              clearStoredCrumb()

              // Try one more time
              Timber.w("Try again after cookie/crumb reset")
              return@withContext attemptAuthedRequest { block(it) }
            }
          }

          throw e
        }
      }

  private suspend fun clearStoredCrumb() = mutex.withLock { storedCrumb = null }

  private fun clearCrumb() {
    scope.launch { clearStoredCrumb() }
  }

  override fun save(cookie: String) {
    preferences.saveYahooCookie(cookie)
  }

  override fun reset() {
    preferences.removeYahooCookie()
    clearCrumb()
  }

  companion object {
    private val MISSING_COOKIE_EXCEPTION =
        RuntimeException(
            "Unable to authorize your device for stock data. Please open the app, which will attempt to refresh the session.")
  }
}
