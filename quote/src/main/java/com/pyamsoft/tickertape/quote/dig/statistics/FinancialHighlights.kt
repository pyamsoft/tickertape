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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Revenue",
        content = financials.totalRevenue.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Revenue per Share",
        content = financials.revenuePerShare.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Quarterly Revenue Growth",
        content = financials.revenueGrowth.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Gross Profit",
        content = financials.grossProfits.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "EBITDA",
        content = financials.ebitda.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Diluted EPS",
        content = info.trailingEps.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Net Income to Common",
        content = info.netIncomeToCommon.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Quarterly Earnings Growth",
        content = financials.earningsGrowth.fmt,
    )
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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Total Cash",
        content = financials.totalCash.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Total Cash per Share",
        content = financials.totalCashPerShare.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Total Debt",
        content = financials.totalDebt.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Total Debt to Equity",
        content = financials.debtToEquity.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Current Ratio",
        content = financials.currentRatio.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Book Value per Share",
        content = info.bookValue.fmt,
    )
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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Operating Cash Flow",
        content = financials.operatingCashflow.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Levered Free Cash Flow",
        content = financials.freeCashflow.fmt,
    )
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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Enterprise Value",
        content = info.enterpriseValue.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Forward P/E",
        content = info.forwardPE.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "PEG Ratio",
        content = info.pegRatio.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Price/Book",
        content = info.priceToBook.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Enterprise Value/Revenue",
        content = info.enterpriseValueToRevenue.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Enterprise Value/EBITDA",
        content = info.enterpriseValueToEbitda.fmt,
    )
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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Previous Fiscal Year",
        content = info.lastFiscalYearEnd.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Fiscal Year Ends",
        content = info.nextFiscalYearEnd.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Most Recent Quarter",
        content = info.mostRecentQuarter.fmt,
    )
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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Profit Margin",
        content = financials.profitMargin.fmt)
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Operating Margin",
        content = financials.operatingMargin.fmt,
    )
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

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Return on Assets",
        content = financials.returnOnAssets.fmt,
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Return on Equity",
        content = financials.returnOnEquity.fmt,
    )
  }
}
