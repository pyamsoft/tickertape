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

package com.pyamsoft.tickertape.alert.work.notification

import com.pyamsoft.pydroid.notify.NotifyData
import com.pyamsoft.tickertape.stocks.api.StockQuote

data class BigMoverNotificationData internal constructor(val quote: StockQuote) : NotifyData {

  companion object {

    const val INTENT_KEY_SYMBOL = "intent_key_symbol"
    const val INTENT_KEY_LOOKUP_SYMBOL = "intent_key_lookup_symbol"
    const val INTENT_KEY_EQUITY_TYPE = "intent_key_equity_type"
  }
}
