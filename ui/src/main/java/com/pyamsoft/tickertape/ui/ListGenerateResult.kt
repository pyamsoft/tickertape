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

package com.pyamsoft.tickertape.ui

import androidx.annotation.CheckResult

interface ListGenerateResult<T : Any> {
  val all: List<T>
  val visible: List<T>

  companion object {

    @JvmStatic
    @CheckResult
    fun <T : Any> create(all: List<T>, visible: List<T>): ListGenerateResult<T> {
      return ListGenerateResultImpl(
          all = all,
          visible = visible,
      )
    }
  }
}

private data class ListGenerateResultImpl<T : Any>(
    override val all: List<T>,
    override val visible: List<T>,
) : ListGenerateResult<T>
