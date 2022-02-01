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
import com.pyamsoft.pydroid.theme.keylines
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
  val isOption = stock.isOption

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
  val totalChangeTitle =
      remember(totalDirection) {
        when {
          totalDirection.isUp() -> "Overall Gain"
          totalDirection.isDown() -> "Overall Loss"
          else -> "Overall Change"
        }
      }

  Box(
      modifier = modifier,
  ) {
    if (ticker != null) {
      Quote(
          modifier = Modifier.fillMaxWidth(),
          ticker = ticker,
          onClick = { onSelect(stock) },
          onLongClick = { onDelete(stock) },
      ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.baseline),
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Info(
                modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
                name = if (isOption) "Contracts" else "Shares",
                value = stock.totalShares.asShareValue(),
            )
            Info(
                name = "Change Today",
                value = stock.changeTodayDisplayString,
                valueColor = todayComposeColor,
            )
          }
          Row(
              modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Info(
                modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
                name = "Value",
                value = stock.current.asMoneyValue())

            Info(
                name = totalChangeTitle,
                value = stock.gainLossDisplayString,
                valueColor = totalComposeColor,
            )
          }
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
