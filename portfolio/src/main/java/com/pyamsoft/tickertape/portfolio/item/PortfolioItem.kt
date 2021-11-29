package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
  Box(
      modifier = modifier,
  ) {
    Quote(
        modifier = Modifier.fillMaxWidth(),
        ticker = ticker.requireNotNull(),
        onClick = { onSelect(stock) },
        onLongClick = { onDelete(stock) },
    )
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
