package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.portfolio.PortfolioStockList

@Composable
@JvmOverloads
fun PorfolioSummaryItem(
    modifier: Modifier = Modifier,
    portfolio: PortfolioStockList,
) {
  val totalAmount = portfolio.sumTotalAmount
  val totalDirection = portfolio.sumTotalDirection
  val changeToday = portfolio.changeTodayDisplayString
  val todayDirection = portfolio.sumTodayDirection
  val gainLoss = portfolio.gainLossDisplayString

  val totalComposeColor =
      if (totalDirection.isZero()) {
        MaterialTheme.typography.caption.color
      } else {
        remember(totalDirection) { Color(totalDirection.color()) }
      }

  val todayComposeColor =
      if (todayDirection.isZero()) {
        MaterialTheme.typography.caption.color
      } else {
        remember(todayDirection) { Color(todayDirection.color()) }
      }

  Column(
      modifier = modifier,
  ) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = totalAmount.asMoneyValue(),
        style = MaterialTheme.typography.h4,
    )

    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = gainLoss,
        style = MaterialTheme.typography.h5.copy(color = totalComposeColor),
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = "Change Today",
          style = MaterialTheme.typography.h6,
      )

      Text(
          modifier = Modifier.padding(start = 8.dp),
          text = changeToday,
          style = MaterialTheme.typography.h6.copy(color = todayComposeColor),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewPorfolioSummaryItem() {
  Surface {
    PorfolioSummaryItem(
        portfolio = PortfolioStockList.empty(),
    )
  }
}
