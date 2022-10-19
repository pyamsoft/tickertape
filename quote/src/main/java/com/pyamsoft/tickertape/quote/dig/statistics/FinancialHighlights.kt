package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockQuote

internal fun LazyListScope.renderFinancialHighlights(
    statistics: KeyStatistics,
) {
  val info = statistics.info
  val quote = statistics.quote
  val financials = statistics.financials

  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Financial Highlights",
        big = true,
    )
  }

  if (info != null && quote != null && financials != null) {
    renderValuationMeasures(
        info = info,
        quote = quote,
    )
  }

  if (info != null) {
    renderFiscalYear(
        info = info,
    )
  }

  if (financials != null) {
    renderProfitability(
        financials = financials,
    )

    renderManagementEffectiveness(
        financials = financials,
    )
  }

  if (info != null && financials != null) {
    renderIncomeStatement(
        info = info,
        financials = financials,
    )
  }

  if (financials != null) {
    renderCashFlowStatement(
        financials = financials,
    )
  }

  if (info != null && financials != null) {
    renderBalanceSheet(
        info = info,
        financials = financials,
    )
  }
}

private fun LazyListScope.renderIncomeStatement(
    info: KeyStatistics.Info,
    financials: KeyStatistics.Financials,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Income Statement",
    )
  }

  financials.totalRevenue.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Revenue",
          content = v,
      )
    }
  }

  financials.revenuePerShare.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Revenue per Share",
          content = v,
      )
    }
  }

  financials.revenueGrowth.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Quarterly Revenue Growth",
          content = v,
      )
    }
  }

  financials.grossProfits.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Gross Profit",
          content = v,
      )
    }
  }

  financials.ebitda.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "EBITDA",
          content = v,
      )
    }
  }

  info.trailingEps.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Diluted EPS",
          content = v,
      )
    }
  }

  info.netIncomeToCommon.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Net Income to Common",
          content = v,
      )
    }
  }

  financials.earningsGrowth.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Quarterly Earnings Growth",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderBalanceSheet(
    info: KeyStatistics.Info,
    financials: KeyStatistics.Financials,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Balance Sheet",
    )
  }

  financials.totalCash.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Total Cash",
          content = v,
      )
    }
  }

  financials.totalCashPerShare.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Total Cash per Share",
          content = v,
      )
    }
  }

  financials.totalDebt.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Total Debt",
          content = v,
      )
    }
  }

  financials.debtToEquity.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Total Debt to Equity",
          content = v,
      )
    }
  }

  financials.currentRatio.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Current Ratio",
          content = v,
      )
    }
  }

  info.bookValue.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Book Value per Share",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderCashFlowStatement(
    financials: KeyStatistics.Financials,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Cash Flow Statement",
    )
  }

  financials.operatingCashflow.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Operating Cash Flow",
          content = v,
      )
    }
  }

  financials.freeCashflow.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Levered Free Cash Flow",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderValuationMeasures(
    info: KeyStatistics.Info,
    quote: StockQuote,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Valuation Measures",
    )
  }

  quote.extraDetails.marketCap?.also { cap ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Market Cap",
          content = cap.display,
      )
    }
  }

  info.enterpriseValue.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Enterprise Value",
          content = v,
      )
    }
  }

  info.forwardPE.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Forward P/E",
          content = v,
      )
    }
  }

  info.pegRatio.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "PEG Ratio",
          content = v,
      )
    }
  }

  info.priceToBook.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Price/Book",
          content = v,
      )
    }
  }

  info.enterpriseValueToRevenue.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Enterprise Value/Revenue",
          content = v,
      )
    }
  }

  info.enterpriseValueToEbitda.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Enterprise Value/EBITDA",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderFiscalYear(
    info: KeyStatistics.Info,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Fiscal Year",
    )
  }

  info.lastFiscalYearEnd.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Previous Fiscal Year",
          content = v,
      )
    }
  }

  info.nextFiscalYearEnd.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Fiscal Year Ends",
          content = v,
      )
    }
  }

  info.mostRecentQuarter.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Most Recent Quarter",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderProfitability(
    financials: KeyStatistics.Financials,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Profitability",
    )
  }

  financials.profitMargin.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Profit Margin",
          content = v,
      )
    }
  }

  financials.operatingMargin.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Operating Margin",
          content = v,
      )
    }
  }
}

private fun LazyListScope.renderManagementEffectiveness(
    financials: KeyStatistics.Financials,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Management Effectiveness",
    )
  }

  financials.returnOnAssets.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Return on Assets",
          content = v,
      )
    }
  }

  financials.returnOnEquity.fmt?.also { v ->
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Return on Equity",
          content = v,
      )
    }
  }
}
