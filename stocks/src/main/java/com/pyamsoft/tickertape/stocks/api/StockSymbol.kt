/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.data.StockSymbolImpl
import java.util.Locale

interface StockSymbol {

  @CheckResult fun symbol(): String
}

@CheckResult
private fun String.asSymbol(locale: Locale): StockSymbol {
  return StockSymbolImpl(this.uppercase(locale))
}

@CheckResult
fun String.asSymbol(): StockSymbol {
  return this.asSymbol(Locale.getDefault())
}
