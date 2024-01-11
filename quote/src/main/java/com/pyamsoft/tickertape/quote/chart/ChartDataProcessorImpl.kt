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

package com.pyamsoft.tickertape.quote.chart

import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.periodHigh
import com.pyamsoft.tickertape.stocks.api.periodLow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class ChartDataProcessorImpl @Inject internal constructor() : ChartDataProcessor {

  override suspend fun processChartEntries(chart: StockChart): ChartDataPainter =
      withContext(context = Dispatchers.Default) {
        val high = chart.periodHigh()
        val low = chart.periodLow()
        val range = chart.range

        val closes = chart.close
        val baseline = chart.startingPrice

        val dataPoints =
            chart.dates.mapIndexed { i, date ->
              val close = closes[i]
              ChartData(
                  high = high,
                  low = low,
                  baseline = baseline,
                  range = range,
                  date = date,
                  price = close,
              )
            }

        // Only one segment, color the chart based on the final price compared to the baseline
        val lineColor =
            dataPoints.lastOrNull().let { lastData ->
              if (lastData == null) DEFAULT_STOCK_COLOR
              else {
                val price = lastData.price.value
                val base = lastData.baseline.value
                when {
                  price < base -> DEFAULT_STOCK_DOWN_COLOR
                  price > base -> DEFAULT_STOCK_UP_COLOR
                  else -> DEFAULT_STOCK_COLOR
                }
              }
            }

        val coordinates =
            dataPoints.mapIndexed { i, d ->
              ChartDataCoordinates(
                  data = d,
                  x = i.toFloat(),
                  y = d.price.value.toFloat(),
              )
            }

        return@withContext ChartDataPainter(
            coordinates = coordinates,
            color = lineColor,
            highMoney = high,
            lowMoney = low,
            startingMoney = baseline,
        )
      }
}
