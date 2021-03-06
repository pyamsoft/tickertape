package com.pyamsoft.tickertape.quote.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
      )
    }
  }
}

@Composable
private fun RecommendationList(
    modifier: Modifier = Modifier,
    recommendations: List<Ticker>,
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
      )
    }
  }
}

@Composable
private fun RecommendationItem(
    modifier: Modifier = Modifier,
    recommendation: Ticker,
) {
  val chart = recommendation.chart

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline),
    ) {
      TickerName(
          modifier = Modifier.fillMaxWidth(),
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
        TickerPrice(
            modifier = Modifier.fillMaxWidth(),
            ticker = recommendation,
            size = TickerSize.QUOTE,
        )
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
  )
}
