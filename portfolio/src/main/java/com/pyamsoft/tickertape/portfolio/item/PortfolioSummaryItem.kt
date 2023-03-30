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

package com.pyamsoft.tickertape.portfolio.item

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.portfolio.PortfolioData
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.asGainLoss
import com.pyamsoft.tickertape.ui.LongTermPurchaseDateTag
import com.pyamsoft.tickertape.ui.ShortTermPurchaseDateTag

@Composable
fun PorfolioSummaryItem(
    modifier: Modifier = Modifier,
    data: PortfolioData.Data,
) {
  DisplayPortfolioData(
      modifier = modifier,
      data = data,
  )
}

@CheckResult
private fun resolveDirectionColor(
    direction: StockDirection,
    colors: Colors,
    alpha: Float,
): Color {
  return if (direction.isZero) {
    colors.onBackground.copy(alpha = alpha)
  } else {
    Color(direction.color)
  }
}

@Composable
private fun DisplayPortfolioData(
    modifier: Modifier = Modifier,
    data: PortfolioData.Data,
) {
  val colors = MaterialTheme.colors
  val mediumAlpha = ContentAlpha.medium

  val totalComposeColor =
      remember(
          data,
          colors,
          mediumAlpha,
      ) {
        resolveDirectionColor(
            data.total.direction,
            colors,
            mediumAlpha,
        )
      }

  val totalGainLoss =
      remember(data) {
        val total = data.total
        val amount = "${total.direction.sign}${total.change.display}"
        val pct = "${total.direction.sign}${total.changePercent.display}"
        return@remember "$amount ($pct)"
      }

  val totalGainLossLabel = remember(data) { data.total.direction.asGainLoss().uppercase() }

  val todayComposeColor =
      remember(
          data,
          colors,
          mediumAlpha,
      ) {
        resolveDirectionColor(
            data.today.direction,
            colors,
            mediumAlpha,
        )
      }

  val todayGainLoss =
      remember(data) {
        val today = data.today
        val amount = "${today.direction.sign}${today.change.display}"
        val pct = "${today.direction.sign}${today.changePercent.display}"
        return@remember "$amount ($pct)"
      }

  val todayGainLossLabel = remember(data) { data.today.direction.asGainLoss().uppercase() }

  val labelColor =
      remember(
          colors,
          mediumAlpha,
      ) {
        colors.onBackground.copy(
            alpha = mediumAlpha,
        )
      }

  Column(
      modifier = modifier.fillMaxWidth(),
  ) {
    Text(
        text = "CURRENT VALUE",
        style =
            MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.W400,
                color = labelColor,
            ),
    )
    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        text = data.current.display,
        style =
            MaterialTheme.typography.h3.copy(
                fontWeight = FontWeight.W700,
            ),
    )

    Text(
        text = "TOTAL $totalGainLossLabel",
        style =
            MaterialTheme.typography.caption.copy(
                fontWeight = FontWeight.W400,
                color = labelColor,
            ),
    )
    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        text = totalGainLoss,
        style =
            MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.W400,
                color = totalComposeColor,
            ),
    )

    Text(
        text = "TODAY'S $todayGainLossLabel",
        style =
            MaterialTheme.typography.caption.copy(
                fontWeight = FontWeight.W400,
                color = labelColor,
            ),
    )
    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        text = todayGainLoss,
        style =
            MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.W400,
                color = todayComposeColor,
            ),
    )

    val pos = data.positions
    Column(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
    ) {
      if (pos.shortTerm > 0 || pos.longTerm > 0) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.typography),
            text = "POSITIONS",
            style =
                MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.W400,
                    color = labelColor,
                ),
        )
      }

      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        if (pos.shortTerm > 0) {
          ShortTermPurchaseDateTag(
              style = MaterialTheme.typography.body2,
          )
          Text(
              modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
              text = "${pos.shortTerm}",
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W400,
                  ),
          )
        }

        if (pos.shortTerm > 0 && pos.longTerm > 0) {
          Spacer(
              modifier = Modifier.width(MaterialTheme.keylines.content),
          )
        }

        if (pos.longTerm > 0) {
          LongTermPurchaseDateTag(
              style = MaterialTheme.typography.body2,
          )
          Text(
              modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
              text = "${pos.longTerm}",
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W400,
                  ),
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewPortfolioSummaryItem() {
  Surface {
    PorfolioSummaryItem(
        data = PortfolioData.EMPTY.stocks,
    )
  }
}
