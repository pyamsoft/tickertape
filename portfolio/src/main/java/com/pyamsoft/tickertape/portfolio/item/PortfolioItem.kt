package com.pyamsoft.tickertape.portfolio.item

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
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.ZeroSize
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.test.newTestHolding
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.QuoteScope
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asGainLoss
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.LongTermPurchaseDateTag
import com.pyamsoft.tickertape.ui.ShortTermPurchaseDateTag

@Composable
@JvmOverloads
internal fun PortfolioItem(
    modifier: Modifier = Modifier,
    stock: PortfolioStock,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
) {
  Quote(
      modifier = modifier.fillMaxWidth(),
      symbol = stock.holding.symbol,
      ticker = stock.ticker,
      onClick = { onSelect(stock) },
      onLongClick = { onDelete(stock) },
  ) {
    Column {
      val shareCount = stock.totalShares
      if (shareCount.isValid && !shareCount.isZero) {
        PositionData(
            stock = stock,
        )
      } else {
        QuoteData(
            ticker = stock.ticker,
        )
      }
    }
  }
}

@Composable
private fun QuoteScope.QuoteData(
    ticker: Ticker?,
) {
  val quote = ticker?.quote

  if (quote != null) {
    Column {
      quote.dayPreviousClose?.also { close ->
        Info(
            name = "Previous Close",
            value = close.display,
        )
      }

      quote.dayOpen?.also { open ->
        Info(
            name = "Open",
            value = open.display,
        )
      }

      quote.dayLow?.also { low ->
        Info(
            name = "Low",
            value = low.display,
        )
      }

      quote.dayHigh?.also { high ->
        Info(
            name = "High",
            value = high.display,
        )
      }

      quote.dayVolume?.also { volume ->
        Info(
            name = "Volume",
            value = volume.display,
        )
      }
    }
  }
}

@Composable
private fun QuoteScope.PositionData(
    stock: PortfolioStock,
) {
  val totalDirection = stock.totalDirection
  val totalChangeTitle = remember(totalDirection) { totalDirection.asGainLoss() }

  Info(
      name = if (stock.isOption) "Contracts" else "Shares",
      value = stock.totalShares.display,
  )

  // These are only valid if we have current day quotes
  if (stock.ticker != null) {
    Info(
        name = "$totalChangeTitle Percent",
        value = stock.totalGainLossPercent,
    )
  } else {
    Info(
        name = "Value",
        value = stock.current.display,
    )
  }

  if (stock.shortTermPositions > 0 || stock.longTermPositions > 0) {
    Info(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.typography),
        name = "Positions",
        // No value here
        value = "",
    )
  }

  if (stock.shortTermPositions > 0) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      ShortTermPurchaseDateTag()
      Info(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          value = "${stock.shortTermPositions}",
      )
    }
  }

  if (stock.longTermPositions > 0) {
    Row(
        modifier =
            Modifier.padding(
                top =
                    if (stock.shortTermPositions > 0) MaterialTheme.keylines.baseline else ZeroSize,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      LongTermPurchaseDateTag()
      Info(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          value = "${stock.longTermPositions}",
      )
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
