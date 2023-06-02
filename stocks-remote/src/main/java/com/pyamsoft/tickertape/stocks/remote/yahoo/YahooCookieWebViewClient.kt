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

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import timber.log.Timber
import javax.inject.Inject

class YahooCookieWebViewClient : WebViewClient() {

  @Inject @JvmField @YahooApi internal var yahooCookieManager: YahooCookieManager? = null

  private fun inject(context: Context) {
    YahooObjectGraph.retrieve(context).inject(this)
  }

  private fun saveCookie(
      context: Context,
      cookies: String,
  ) {
    if (yahooCookieManager == null) {
      inject(context)
    }

    yahooCookieManager.requireNotNull().also { y ->
      if (cookies.isBlank()) {
        Timber.d("Reset stored cookie")
        y.reset()
      } else {
        Timber.d("Save cookie to storage $cookies")
        y.save(cookies)
      }
    }
  }

  override fun onPageFinished(view: WebView, url: String) {
    super.onPageFinished(view, url)
    val cookies = CookieManager.getInstance().getCookie(url)
    saveCookie(view.context.applicationContext, cookies)
  }
}
