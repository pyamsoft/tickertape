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

package com.pyamsoft.tickertape.stocks.remote

import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.scope.StockApi
import com.squareup.moshi.Moshi
import javax.inject.Inject

internal class MoshiJsonParser
@Inject
constructor(
    @StockApi private val moshi: Moshi,
) : JsonParser {

  override fun <T : Any> toJson(data: T): String {
    return moshi.adapter<T>(data::class.java).toJson(data)
  }

  override fun <T : Any> fromJson(json: String, type: Class<T>): T? {
    return moshi.adapter(type).fromJson(json)
  }
}
