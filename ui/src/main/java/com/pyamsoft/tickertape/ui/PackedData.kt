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

package com.pyamsoft.tickertape.ui

import androidx.annotation.CheckResult

/** A [PackedData] object can either be data or an error */
sealed class PackedData<T> {
  data class Data<T>(val value: T) : PackedData<T>()
  data class Error<T>(val throwable: Throwable) : PackedData<T>()
}

@CheckResult
fun <T> T.pack(): PackedData<T> {
  return PackedData.Data(value = this)
}

@CheckResult
fun <T> Throwable.packError(): PackedData<T> {
  return PackedData.Error(throwable = this)
}

@CheckResult
inline fun <T> PackedData<T>.transformData(block: (T) -> T): PackedData<T> {
  return when (this) {
    is PackedData.Data -> this.copy(value = block(this.value))
    is PackedData.Error -> this
  }
}
