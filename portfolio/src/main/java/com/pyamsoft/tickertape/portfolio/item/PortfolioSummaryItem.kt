package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
@JvmOverloads
fun PorfolioSummaryItem(
    modifier: Modifier = Modifier,
    portfolio: PortfolioStockList,
    equityType: EquityType,
) {
  // Short circuit
  val data = remember(portfolio, equityType) { portfolio.generateData(equityType) } ?: return

  val typography = MaterialTheme.typography

  val totalComposeColor =
      remember(data.total.direction, typography) {
        val direction = data.total.direction
        if (direction.isZero) {
          typography.caption.color
        } else {
          Color(direction.color)
        }
      }

  val totalGainLoss =
      remember(data.total) {
        val total = data.total
        val amount = "${total.direction.sign}${total.change.display}"
        val pct = "${total.direction.sign}${total.changePercent.display}"
        return@remember "$amount ($pct)"
      }

  val todayComposeColor =
      remember(data.today.direction, typography) {
        val direction = data.today.direction
        if (direction.isZero) {
          typography.caption.color
        } else {
          Color(direction.color)
        }
      }

  val todayGainLoss =
      remember(data.today) {
        val today = data.today
        val amount = "${today.direction.sign}${today.change.display}"
        val pct = "${today.direction.sign}${today.changePercent.display}"
        return@remember "$amount ($pct)"
      }

  Column(
      modifier = modifier.fillMaxWidth(),
  ) {
    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        text = data.current.display,
        style = MaterialTheme.typography.h3,
    )

    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        text = totalGainLoss,
        style = MaterialTheme.typography.h5.copy(color = totalComposeColor),
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = "Today",
          style = MaterialTheme.typography.h6,
      )

      Text(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          text = todayGainLoss,
          style = MaterialTheme.typography.h6.copy(color = todayComposeColor),
      )
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
