package com.pyamsoft.tickertape.quote.dig.recommend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerName
import com.pyamsoft.tickertape.quote.TickerPrice
import com.pyamsoft.tickertape.quote.TickerSize
import com.pyamsoft.tickertape.quote.chart.Chart
import com.pyamsoft.tickertape.quote.dig.DigDefaults
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.RecommendationDigViewState
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.BorderCard

@Composable
fun DigRecommendations(
    modifier: Modifier = Modifier,
    state: RecommendationDigViewState,
    onRefresh: () -> Unit,
    onRecClick: (Ticker) -> Unit,
) {
  val error = state.recommendationError

  SwipeRefresh(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {
      if (error == null) {
        items(
            items = state.recommendations,
            key = { it.symbol.raw },
        ) { rec ->
          RecommendationItem(
              modifier = Modifier.fillMaxWidth(),
              recommendation = rec,
              onClick = onRecClick,
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

@Composable
private fun RecommendationItem(
    modifier: Modifier = Modifier,
    recommendation: Ticker,
    onClick: (Ticker) -> Unit,
) {
  val chart = recommendation.chart

  val isRecommendationSpecialSession =
      remember(recommendation) {
        val quote = recommendation.quote
        if (quote == null) {
          return@remember false
        } else {
          return@remember quote.afterHours != null || quote.preMarket != null
        }
      }

  BorderCard(
      modifier = modifier,
  ) {
    Column(
        modifier =
            Modifier.clickable { onClick(recommendation) }.padding(MaterialTheme.keylines.baseline),
    ) {
      TickerName(
          modifier = Modifier.fillMaxWidth(),
          symbol = recommendation.symbol,
          ticker = recommendation,
          size = TickerSize.QUOTE,
      )
      if (chart != null) {
        Chart(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.baseline)
                    .height(DigDefaults.rememberChartHeight()),
            chart = chart,
        )
      }
      Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.BottomEnd,
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
        ) {
          if (isRecommendationSpecialSession) {
            TickerPrice(
                modifier = Modifier.padding(end = MaterialTheme.keylines.content),
                ticker = recommendation,
                size = TickerSize.RECOMMEND_QUOTE_EXTRA,
            )
          }

          TickerPrice(
              ticker = recommendation,
              size = TickerSize.RECOMMEND_QUOTE,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDigRecommendations() {
  DigRecommendations(
      state =
          object :
              MutableDigViewState(
                  symbol = "MSFT".asSymbol(),
              ) {},
      onRefresh = {},
      onRecClick = {},
  )
}
