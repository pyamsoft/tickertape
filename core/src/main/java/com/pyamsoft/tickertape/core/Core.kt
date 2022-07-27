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

package com.pyamsoft.tickertape.core

import android.graphics.Color
import androidx.annotation.ColorInt

const val PRIVACY_POLICY_URL = "https://pyamsoft.blogspot.com/p/tickertape-privacy-policy.html"
const val TERMS_CONDITIONS_URL =
    "https://pyamsoft.blogspot.com/p/tickertape-terms-and-conditions.html"

@ColorInt const val DEFAULT_STOCK_COLOR = Color.WHITE
@ColorInt val DEFAULT_STOCK_UP_COLOR = Color.parseColor("#388E3C")
@ColorInt val DEFAULT_STOCK_DOWN_COLOR = Color.parseColor("#D32F2F")
