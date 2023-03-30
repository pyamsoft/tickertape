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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue

@Composable
internal fun ChartBounds(
    modifier: Modifier = Modifier,
    painter: ChartDataPainter,
) {
  val highMoney = painter.highMoney
  val lowMoney = painter.lowMoney

  Column(
      modifier = modifier,
  ) {
    if (highMoney.isValid) {
      ChartBound(
          money = highMoney,
      )
    }

    Spacer(
        modifier = Modifier.weight(1F),
    )

    if (lowMoney.isValid) {
      ChartBound(
          money = lowMoney,
      )
    }
  }
}

@Composable
private fun ChartBound(
    modifier: Modifier = Modifier,
    money: StockMoneyValue,
) {
  Surface(
      modifier = modifier,
      shape = MaterialTheme.shapes.small,
      color = MaterialTheme.colors.primary,
      contentColor = Color.White,
  ) {
    Text(
        modifier =
            Modifier.padding(
                horizontal = MaterialTheme.keylines.typography,
                vertical = MaterialTheme.keylines.typography / 2,
            ),
        text = money.display,
        style = MaterialTheme.typography.overline,
    )
  }
}
