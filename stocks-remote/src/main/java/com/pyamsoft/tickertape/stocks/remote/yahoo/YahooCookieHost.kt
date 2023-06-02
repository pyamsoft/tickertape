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

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isGone
import com.pyamsoft.pydroid.theme.ZeroSize

@Composable
fun YahooCookieHost(modifier: Modifier = Modifier) {
  val (knownView, setKnownView) = remember { mutableStateOf<WebView?>(null) }

  DisposableEffect(knownView) { onDispose { knownView?.destroy() } }
  LaunchedEffect(knownView) { knownView?.loadUrl("https://finance.yahoo.com") }

  AndroidView(
      modifier = modifier.size(ZeroSize),
      factory = { context ->
        WebView(context).apply {
          setKnownView(this)

          // Hide the view
          layoutParams = ViewGroup.LayoutParams(0, 0)
          isGone = true

          webViewClient = YahooCookieWebViewClient()
        }
      },
  )
}
