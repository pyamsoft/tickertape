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

package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import com.pyamsoft.spark.SparkView
import timber.log.Timber

@CheckResult
fun createChartLogger(): SparkView.Logger =
    object : SparkView.Logger {
      override fun d(tag: String, message: String, vararg args: Any?) {
        Timber.tag(tag).d(message, args)
      }

      override fun w(tag: String, message: String, vararg args: Any?) {
        Timber.tag(tag).w(message, args)
      }

      override fun e(tag: String, throwable: Throwable, message: String, vararg args: Any?) {
        Timber.tag(tag).e(throwable, message, args)
      }
    }
