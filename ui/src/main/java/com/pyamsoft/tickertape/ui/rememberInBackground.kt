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

package com.pyamsoft.tickertape.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Compute the value of some data in the background and remember the result */
@Composable
fun <T : Any> rememberInBackground(
    vararg arguments: Any?,
    onCalculate: CoroutineScope.() -> T?,
): T? {
  // Generate the display values in the background
  val (value, setValue) = remember { mutableStateOf<T?>(null) }

  // Avoid firing the launched effect again on each calculate loop
  val handleCalculation by rememberUpdatedState(onCalculate)

  LaunchedEffect(keys = arguments) {
    val scope = this

    // Default for computation intensive task
    scope.launch(context = Dispatchers.Default) {
      val result = handleCalculation()
      setValue(result)
    }
  }

  return value
}
