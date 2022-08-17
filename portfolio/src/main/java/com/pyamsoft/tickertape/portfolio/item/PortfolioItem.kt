package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.test.newTestHolding
import com.pyamsoft.tickertape.quote.CRYPTO_LIMIT_PERCENT
import com.pyamsoft.tickertape.quote.OPTIONS_LIMIT_PERCENT
import com.pyamsoft.tickertape.quote.QUOTE_DEFAULT_LIMIT_PERCENT
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.rememberCardBackgroundColorForPercentChange
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.EquityType
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
  val isOption = stock.isOption

  // Color the portfolio card based on the percent move today
  // If this is a sell side position, we flip the percentage direction
  val percentChange = remember(stock) { stock.todayPercent * if (stock.isSell) -1 else 1 }
    // Decide limits based on equity type
  val limitPercent =
      remember(stock) {
        when (stock.holding.type) {
          EquityType.STOCK -> QUOTE_DEFAULT_LIMIT_PERCENT
          EquityType.OPTION -> OPTIONS_LIMIT_PERCENT
          EquityType.CRYPTOCURRENCY -> CRYPTO_LIMIT_PERCENT
        }
      }

  val totalChangeTitle =
      remember(totalDirection) {
        when {
          totalDirection.isUp -> "Gain"
          totalDirection.isDown -> "Loss"
          else -> "Change"
        }
      }

  if (ticker != null) {
    Quote(
        modifier = modifier.fillMaxWidth(),
        ticker = ticker,
        backgroundColor =
            rememberCardBackgroundColorForPercentChange(
                percentChange = percentChange,
                changeLimit = limitPercent,
            ),
        onClick = { onSelect(stock) },
        onLongClick = { onDelete(stock) },
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.baseline),
      ) {
        Info(
            name = if (isOption) "Contracts" else "Shares",
            value = stock.totalShares.display,
        )

        Info(
            name = "Value",
            value = stock.current.display,
        )

        Info(
            name = "Change Today",
            value = stock.changeTodayDisplayString,
        )

        Info(
            modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
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
