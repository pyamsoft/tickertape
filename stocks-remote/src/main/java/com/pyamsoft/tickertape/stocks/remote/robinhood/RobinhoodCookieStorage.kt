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

package com.pyamsoft.tickertape.stocks.remote.robinhood

import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.stocks.remote.api.RobinhoodApi
import com.pyamsoft.tickertape.stocks.remote.storage.AbstractStorage
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

@Singleton
internal class RobinhoodCookieStorage
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @RobinhoodApi private val service: RobinhoodCookieService,
    private val clock: Clock,
) :
    AbstractStorage<RobinhoodToken>(
        enforcer = enforcer,
    ) {

  override suspend fun getToken(): RobinhoodToken {
    val token = service.getToken()
    return RobinhoodToken(
        accessToken = "Bearer ${token.accessToken}",
        expiresInMilliseconds = token.expiresInMilliseconds,
    )
  }

  override suspend fun validateToken(token: RobinhoodToken): Boolean {
    return token.expiresAt > LocalDateTime.now(clock)
  }

  override suspend fun <T : Any> withAuth(block: suspend (RobinhoodToken) -> T): T =
      withContext(context = Dispatchers.Default) {
        try {
          return@withContext attemptAuthedRequest { block(it) }
        } catch (e: Throwable) {
          if (e is HttpException) {
            if (e.code() == 401) {
              Timber.w("RH returned a 401. We have a bad cookie or crumb.")

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
}
