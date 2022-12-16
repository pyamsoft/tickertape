package com.pyamsoft.tickertape.portfolio.item

import androidx.annotation.CheckResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.asGainLoss

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
      modifier = modifier,
      visible = data != null,
      enter = fadeIn() + slideInVertically(),
      exit = slideOutVertically() + fadeOut(),
  ) {
    DisplayPortfolioData(
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
