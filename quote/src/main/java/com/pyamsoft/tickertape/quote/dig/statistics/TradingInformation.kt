/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockQuote

internal fun LazyListScope.renderTradingInformation(
    statistics: KeyStatistics,
) {
  val info = statistics.info
  val quote = statistics.quote
  val earnings = statistics.earnings

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

  if (earnings != null && info != null && quote != null) {
    renderDividendsAndSplits(
        earnings = earnings,
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

  info.beta.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Beta",
          content = v,
      )
    }
  }

  info.fiftyTwoWeekChange.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "52-Week Change",
          content = v,
      )
    }
  }

  info.marketFiftyTwoWeekChange.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "S&P500 52-Week Change",
          content = v,
      )
    }
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

  info.sharesOutstanding.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Shares Outstanding",
          content = v,
      )
    }
  }

  info.floatShares.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Float",
          content = v,
      )
    }
  }

  info.heldPercentInsiders.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "% Held by Insiders",
          content = v,
      )
    }
  }

  info.heldPercentInstitutions.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "% Held by Institutions",
          content = v,
      )
    }
  }

  info.sharesShort.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Shares Short",
          content = v,
      )
    }
  }

  info.shortRatio.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Short Ratio",
          content = v,
      )
    }
  }

  info.shortPercentOfFloat.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Short % of Float",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderDividendsAndSplits(
    earnings: KeyStatistics.Earnings,
    info: KeyStatistics.Info,
    quote: StockQuote,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Dividends & Splits",
    )
  }

  quote.extraDetails.trailingAnnualDividendRate?.also { v ->
    item {
      val formatted = remember(v) { "%.2f".format(v) }
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Trailing Annual Dividend Rate",
          content = formatted,
      )
    }
  }

  quote.extraDetails.trailingAnnualDividendYield?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Trailing Annual Dividend Yield",
          content = v.display,
      )
    }
  }

  if (!earnings.dividendDate.isEmpty) {
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Dividend Date",
          content = rememberParsedDate(earnings.dividendDate),
      )
    }
  }

  if (!earnings.exDividendDate.isEmpty) {
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Ex-Dividend Date",
          content = rememberParsedDate(earnings.exDividendDate),
      )
    }
  }

  if (info.lastSplitFactor.isNotBlank()) {
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Last Split Factor",
          content = info.lastSplitFactor,
      )
    }
  }

  if (!info.lastSplitDate.isEmpty) {
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Last Split Date",
          content = rememberParsedDate(info.lastSplitDate),
      )
    }
  }
}
