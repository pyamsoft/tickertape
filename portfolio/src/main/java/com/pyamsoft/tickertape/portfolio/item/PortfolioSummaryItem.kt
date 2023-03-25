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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.asGainLoss
import com.pyamsoft.tickertape.ui.LongTermPurchaseDateTag
import com.pyamsoft.tickertape.ui.ShortTermPurchaseDateTag

@Composable
fun PorfolioSummaryItem(
    modifier: Modifier = Modifier,
    portfolio: PortfolioStockList,
    equityType: EquityType,
) {
  // Short circuit
  val data =
      remember(
          portfolio,
          equityType,
      ) {
        portfolio.generateData(equityType)
      }

  AnimatedVisibility(
      visible = data != null,
      enter = fadeIn() + slideInVertically(),
      exit = slideOutVertically() + fadeOut(),
  ) {
    DisplayPortfolioData(
        modifier = modifier,
        data = data,
    )
  }
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
    data: PortfolioStockList.Data?,
) {
  val colors = MaterialTheme.colors
  val mediumAlpha = ContentAlpha.medium

  val totalComposeColor =
      remember(
          data?.total?.direction,
          colors,
          mediumAlpha,
      ) {
        val d = data?.total?.direction
        if (d == null) Color.Unspecified
        else {
          resolveDirectionColor(
              d,
              colors,
              mediumAlpha,
          )
        }
      }

  val totalGainLoss =
      remember(data?.total) {
        val total = data?.total
        if (total == null) {
          return@remember ""
        } else {
          val amount = "${total.direction.sign}${total.change.display}"
          val pct = "${total.direction.sign}${total.changePercent.display}"
          return@remember "$amount ($pct)"
        }
      }

  val totalGainLossLabel =
      remember(data?.total) {
        val total = data?.total
        if (total == null) {
          return@remember ""
        } else {
          return@remember total.direction.asGainLoss().uppercase()
        }
      }

  val todayComposeColor =
      remember(
          data?.today?.direction,
          colors,
          mediumAlpha,
      ) {
        val d = data?.today?.direction
        if (d == null) Color.Unspecified
        else {
          resolveDirectionColor(
              d,
              colors,
              mediumAlpha,
          )
        }
      }

  val todayGainLoss =
      remember(data?.today) {
        val today = data?.today
        if (today == null) {
          return@remember ""
        } else {
          val amount = "${today.direction.sign}${today.change.display}"
          val pct = "${today.direction.sign}${today.changePercent.display}"
          return@remember "$amount ($pct)"
        }
      }

  val todayGainLossLabel =
      remember(data?.today) {
        val today = data?.today
        if (today == null) {
          return@remember ""
        } else {
          return@remember today.direction.asGainLoss().uppercase()
        }
      }

  val labelColor =
      remember(
          colors,
          mediumAlpha,
      ) {
        colors.onBackground.copy(
            alpha = mediumAlpha,
        )
      }

  val positions =
      remember(data?.positions) {
        val positions = data?.positions
        if (positions == null) {
          return@remember ""
        } else {
          return@remember "Short Term: ${positions.shortTerm} Long Term: ${positions.longTerm}"
        }
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
        text = data?.current?.display.orEmpty(),
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

    val pos = data?.positions
    if (pos != null) {
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
}

@Preview
@Composable
private fun PreviewPortfolioSummaryItem() {
  Surface {
    PorfolioSummaryItem(
        portfolio = PortfolioStockList.empty(),
        equityType = EquityType.STOCK,
    )
  }
}
