package com.pyamsoft.tickertape.quote.dig

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerName
import com.pyamsoft.tickertape.quote.TickerPrice
import com.pyamsoft.tickertape.quote.TickerSize

@Composable
fun DigRecommendations(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    recommendations: List<Ticker>,
    onRefresh: () -> Unit,
    onRecClick: (Ticker) -> Unit,
) {
  val visible = remember(recommendations) { recommendations.isNotEmpty() }

  AnimatedVisibility(
      modifier = Modifier.padding(MaterialTheme.keylines.content),
      visible = visible,
  ) {
    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(isRefreshing = isLoading),
        onRefresh = onRefresh,
    ) {
      RecommendationList(
          modifier = Modifier.fillMaxSize(),
          recommendations = recommendations,
          onRecClick = onRecClick,
      )
    }
  }
}

@Composable
private fun RecommendationList(
    modifier: Modifier = Modifier,
    recommendations: List<Ticker>,
    onRecClick: (Ticker) -> Unit,
) {
  LazyColumn(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
  ) {
    items(
        items = recommendations,
        key = { it.symbol.raw },
    ) { rec ->
      RecommendationItem(
          modifier = Modifier.fillMaxWidth(),
          recommendation = rec,
          onClick = onRecClick,
      )
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

  Card(
      modifier = modifier.clickable { onClick(recommendation) },
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline),
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
                    .height(DigDefaults.getChartHeight()),
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
                size = TickerSize.QUOTE_SPECIAL,
            )
          }

          TickerPrice(
              ticker = recommendation,
              size = TickerSize.QUOTE,
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
      isLoading = false,
      recommendations = emptyList(),
      onRefresh = {},
      onRecClick = {},
  )
}
