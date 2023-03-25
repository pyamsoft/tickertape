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

package com.pyamsoft.tickertape.notification.components

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
@CheckResult
internal fun rememberContentDescription(
    isChecked: Boolean,
    contentDescription: String?,
    contentDescriptionOn: String?,
    contentDescriptionOff: String?
): String? =
    remember(
        isChecked,
        contentDescription,
        contentDescriptionOn,
        contentDescriptionOff,
    ) {
      // If all null, fast path
      if (contentDescription == null &&
          contentDescriptionOff == null &&
          contentDescriptionOn == null) {
        return@remember null
      }

      // If specifics are null, just return general
      if (contentDescriptionOff == null && contentDescriptionOn == null) {
        return@remember contentDescription
      }

      return@remember if (isChecked) {
        contentDescriptionOn ?: contentDescription
      } else {
        contentDescriptionOff ?: contentDescription
      }
    }
