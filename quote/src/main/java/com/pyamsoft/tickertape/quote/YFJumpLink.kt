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

package com.pyamsoft.tickertape.quote

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.icon.OpenInNew

@Composable
fun YFJumpLink(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
) {
  val uriHandler = LocalUriHandler.current

  IconButton(
      modifier = modifier,
      onClick = { uriHandler.openUri("https://finance.yahoo.com/quote/${symbol.raw}") },
  ) {
    Icon(
        imageVector = Icons.Filled.OpenInNew,
        contentDescription = "View ${symbol.raw} on Yahoo Finance",
    )
  }
}
