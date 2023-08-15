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
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.storage.AbstractStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber

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

  /**
   * Simplified
   *
   * https://github.com/gadicc/node-yahoo-finance2/blob/devel/src/lib/getCrumb.ts
   */
  @CheckResult
  private fun potentiallyExtractCrumbFromWindowContext(body: String): String {
    val maybeJson = YF_CONTEXT_REGEX.findAll(body)
    if (maybeJson.none()) {
      Timber.w("Failed to find cookie in YF Context: ${YF_CONTEXT_REGEX.pattern}")
      return ""
    }

    // We need just the JSON object part
    val jsonLike = maybeJson.first().value.replace("window.YAHOO.context = ", "")

    return try {
      JSONObject(jsonLike).getString("crumb")
    } catch (e: JSONException) {
      Timber.e(e, "Error parsing JSON for window.YAHOO.context")
      ""
    }
  }

  override suspend fun getToken(): YahooCrumb {
    val page = service.getCookie(accept = YF_ACCEPT_STRING)
    val cookies = page.headers().values("Set-Cookie")
    val cookieString = cookies.joinToString(";")

    // The crumb may be in the page
    var crumb = potentiallyExtractCrumbFromWindowContext(page.body().requireNotNull())
    if (crumb.isBlank()) {
      // Generally we get 429 from this fallback - but maybe sometimes it will work?
      crumb = service.getCrumb(cookie = cookieString)
    }

    return YahooCrumb(
        cookie = cookieString,
        crumb = crumb,
    )
  }

  override suspend fun validateToken(token: YahooCrumb): Boolean {
    return true
  }

  override suspend fun <T : Any> withAuth(block: suspend (YahooCrumb) -> T): T =
      withContext(context = Dispatchers.Default) {
        try {
          return@withContext attemptAuthedRequest { block(it) }
        } catch (e: Throwable) {
          if (e is HttpException) {
            val code = e.code()
            if (code == 401) {
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

    // YF stores it's crumb in the window.YAHOO.context object
    // We get the page as a String and need to parse it to get the actual JSON with the crumb
    // string.
    // Yeah.
    private val YF_CONTEXT_REGEX = "\nwindow\\.YAHOO\\.context = ([{][\\s\\S]+\n[}]);\n".toRegex()
  }
}
