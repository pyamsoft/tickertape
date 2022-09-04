package com.pyamsoft.tickertape.quote.dig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.HairlineSize
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
fun DigKeyStatistics(
    modifier: Modifier = Modifier,
    state: DigViewState,
    onRefresh: () -> Unit,
) {
  val error = state.statisticsError

  SwipeRefresh(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
      if (error == null) {
        state.statistics?.let { stats ->
          renderFinancialHighlights(
              statistics = stats,
          )
          renderTradingInformation(
              statistics = stats,
          )
        }
      } else {
        item {
          val errorMessage = remember(error) { error.message ?: "An unexpected error occurred" }

          Text(
              text = errorMessage,
              style =
                  MaterialTheme.typography.h6.copy(
                      color = MaterialTheme.colors.error,
                  ),
          )
        }
      }
    }
  }
}

private fun LazyListScope.renderFinancialHighlights(
    statistics: KeyStatistics,
) {
  val info = statistics.info
  val financials = statistics.financials

  if (info != null) {
    renderValuationMeasures(
        info = info,
    )
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
}

private fun LazyListScope.renderValuationMeasures(
    info: KeyStatistics.Info,
) {
  item {
    StatisticsTitle(
        modifier = Modifier.fillMaxWidth(),
        title = "Valuation Measures",
    )
  }

  item {
    StatisticsItem(
        modifier = Modifier.fillMaxWidth(),
        title = "Market Cap",
        content = info.marketCap.fmt,
    )
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
        title = "Management Effectivenss",
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

private fun LazyListScope.renderTradingInformation(
    statistics: KeyStatistics,
) {}

private fun LazyListScope.renderStockPriceHistory(
    info: KeyStatistics.Info,
    financials: KeyStatistics.Financials,
    earnings: KeyStatistics.Financials,
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

@Composable
private fun StatisticsTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline),
        text = title,
        style = MaterialTheme.typography.h6,
    )
  }
}

private val ITEM_HEIGHT = 48.dp

@Composable
private fun StatisticsItem(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
) {
  val borderColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
  val border = remember(borderColor) { BorderStroke(HairlineSize, borderColor) }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier =
            Modifier.fillMaxWidth(fraction = 0.4F)
                .height(ITEM_HEIGHT)
                .border(border)
                .padding(MaterialTheme.keylines.baseline),
        text = title,
        style = MaterialTheme.typography.caption,
    )

    Text(
        modifier =
            Modifier.fillMaxWidth()
                .height(ITEM_HEIGHT)
                .border(border)
                .padding(MaterialTheme.keylines.baseline),
        text = content,
        style = MaterialTheme.typography.body1,
    )
  }
}

@Preview
@Composable
private fun PreviewDigKeyStatistics() {
  DigKeyStatistics(
      state =
          object :
              MutableDigViewState(
                  symbol = "MSFT".asSymbol(),
              ) {},
      onRefresh = {},
  )
}
