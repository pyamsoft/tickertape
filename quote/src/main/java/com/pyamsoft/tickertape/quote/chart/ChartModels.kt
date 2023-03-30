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

package com.pyamsoft.tickertape.quote.chart

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import java.time.LocalDateTime

@Stable
data class ChartDataPainter
internal constructor(
    val coordinates: List<ChartDataCoordinates>,
    @ColorInt val color: Int,
    val highMoney: StockMoneyValue,
    val lowMoney: StockMoneyValue,
    val startingMoney: StockMoneyValue,
) {

  companion object {

    @JvmField
    val EMPTY =
        ChartDataPainter(
            coordinates = emptyList(),
            color = Color.WHITE,
            highMoney = StockMoneyValue.NONE,
            lowMoney = StockMoneyValue.NONE,
            startingMoney = StockMoneyValue.NONE,
        )
  }
}

@Stable
data class ChartDataCoordinates
internal constructor(
    val data: ChartData,
    val x: Float,
    val y: Float,
)

@Stable
data class ChartData(
    val high: StockMoneyValue,
    val low: StockMoneyValue,
    val baseline: StockMoneyValue,
    val date: LocalDateTime,
    val price: StockMoneyValue,
    val range: StockChart.IntervalRange,
)
