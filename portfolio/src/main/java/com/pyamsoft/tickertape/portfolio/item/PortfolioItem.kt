package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.test.newTestHolding
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PortfolioItem(
    modifier: Modifier = Modifier,
    stock: PortfolioStock,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
) {
  val ticker = stock.ticker
  val totalDirection = stock.totalDirection
  val todayDirection = stock.todayDirection

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

  Box(
      modifier = modifier,
  ) {
    Quote(
        modifier = Modifier.fillMaxWidth(),
        ticker = ticker.requireNotNull(),
        onClick = { onSelect(stock) },
        onLongClick = { onDelete(stock) },
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Info(
              modifier = Modifier.padding(end = 8.dp),
              name = "Shares",
              value = stock.totalShares.asShareValue(),
          )
          Info(
              name = "Today",
              value = stock.changeTodayDisplayString,
              valueColor = todayComposeColor,
          )
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Info(
              modifier = Modifier.padding(end = 8.dp),
              name = "Current",
              value = stock.current.asMoneyValue())
          Info(
              name = "Total",
              value = stock.gainLossDisplayString,
              valueColor = totalComposeColor,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewPortfolioItem() {
  val symbol = "MSFT".asSymbol()
  Surface {
    PortfolioItem(
        stock =
            PortfolioStock(
                holding = newTestHolding(symbol),
                positions = emptyList(),
                ticker =
                    Ticker(
                        symbol = symbol,
                        quote = newTestQuote(symbol),
                        chart = null,
                    ),
            ),
        onSelect = {},
        onDelete = {},
    )
  }
}
