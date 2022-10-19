package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.test.newTestHolding
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asGainLoss
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PortfolioItem(
    modifier: Modifier = Modifier,
    stock: PortfolioStock,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
) {
  val totalDirection = stock.totalDirection
  val isOption = stock.isOption

  val totalChangeTitle = remember(totalDirection) { totalDirection.asGainLoss() }

  Quote(
      modifier = modifier.fillMaxWidth(),
      symbol = stock.holding.symbol,
      ticker = stock.ticker,
      onClick = { onSelect(stock) },
      onLongClick = { onDelete(stock) },
  ) {
    Column {
      Info(
          name = if (isOption) "Contracts" else "Shares",
          value = stock.totalShares.display,
      )

      Info(
          name = "Value",
          value = stock.current.display,
      )

      // These are only valid if we have current day quotes
      if (stock.ticker != null) {
        Info(
            name = "Change Today",
            value = stock.changeTodayDisplayString,
        )

        Info(
            name = "$totalChangeTitle Amount",
            value = stock.totalGainLossAmount,
        )

        Info(
            name = "$totalChangeTitle Percent",
            value = stock.totalGainLossPercent,
        )
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
                splits = emptyList(),
            ),
        onSelect = {},
        onDelete = {},
    )
  }
}
