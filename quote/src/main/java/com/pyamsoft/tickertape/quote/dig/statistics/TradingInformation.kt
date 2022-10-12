package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockQuote

internal fun LazyListScope.renderTradingInformation(
    statistics: KeyStatistics,
) {
  val info = statistics.info
  val quote = statistics.quote

  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Trading Information",
        big = true,
    )
  }

  if (info != null && quote != null) {
    renderStockPriceHistory(
        info = info,
        quote = quote,
    )

    renderShareStatistics(
        info = info,
        quote = quote,
    )
  }
}

private fun LazyListScope.renderStockPriceHistory(
    info: KeyStatistics.Info,
    quote: StockQuote,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Stock Price History",
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Beta",
        content = info.beta.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "52-Week Change",
        content = info.fiftyTwoWeekChange.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "S&P500 52-Week Change",
        content = info.marketFiftyTwoWeekChange.fmt,
    )
  }

  quote.extraDetails.fiftyTwoWeekHigh?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "52-Week High",
          content = v.display,
      )
    }
  }

  quote.extraDetails.fiftyTwoWeekLow?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "52-Week Low",
          content = v.display,
      )
    }
  }

  quote.extraDetails.fiftyDayAverage?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "50-Day Moving Average",
          content = v.display,
      )
    }
  }

  quote.extraDetails.twoHundredDayAverage?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "200-Day Moving Average",
          content = v.display,
      )
    }
  }
}

private fun LazyListScope.renderShareStatistics(
    info: KeyStatistics.Info,
    quote: StockQuote,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Share Statistics",
    )
  }

  quote.extraDetails.averageDailyVolume10Day?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "10-Day Average Volume",
          content = v.display,
      )
    }
  }

  quote.extraDetails.averageDailyVolume3Month?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "3-Month Average Volume",
          content = v.display,
      )
    }
  }

  quote.extraDetails.averageDailyVolume3Month?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "3-Month Average Volume",
          content = v.display,
      )
    }
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Shares Outstanding",
        content = info.sharesOutstanding.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Float",
        content = info.floatShares.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "% Held by Insiders",
        content = info.heldPercentInsiders.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "% Held by Institutions",
        content = info.heldPercentInstitutions.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Shares Short",
        content = info.sharesShort.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Shares Ratio",
        content = info.shortRatio.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Shares % of Float",
        content = info.shortPercentOfFloat.fmt,
    )
  }
}
