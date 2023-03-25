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

package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Stable
@Immutable
enum class TickerSize(val isSpecial: Boolean) {
  CHART(isSpecial = false),

  // Quotes
  QUOTE(isSpecial = false),
  QUOTE_EXTRA(isSpecial = true),

  // Recommendations
  RECOMMEND_QUOTE(isSpecial = false),
  RECOMMEND_QUOTE_EXTRA(isSpecial = true)
}

@Stable
data class TickerSizes
internal constructor(
    val title: TextStyle,
    val description: TextStyle,
    val label: TextStyle,
) {

  companion object {
    @JvmStatic
    @CheckResult
    fun chart(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) =
        TickerSizes(
            title =
                typography.h6.copy(
                    color = color.copy(alpha = alphaHigh),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body2.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun quote(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) =
        TickerSizes(
            title =
                typography.h5.copy(
                    color = color.copy(alpha = alphaHigh),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body1.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun price(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) = quote(typography, color, alphaHigh, alphaMedium)

    @JvmStatic
    @CheckResult
    fun priceExtra(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) = chart(typography, color, alphaHigh, alphaMedium)

    @JvmStatic
    @CheckResult
    fun recPrice(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) = priceExtra(typography, color, alphaHigh, alphaMedium)

    @JvmStatic
    @CheckResult
    fun recPriceExtra(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) =
        TickerSizes(
            title =
                typography.body1.copy(
                    color = color.copy(alpha = alphaHigh),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.caption.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.overline.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
        )
  }
}
