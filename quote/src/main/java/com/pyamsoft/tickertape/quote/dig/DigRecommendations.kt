package com.pyamsoft.tickertape.quote.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol

@Composable
fun DigRecommendations(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    recommendations: StockRecommendations?,
    onRefresh: () -> Unit,
) {
  val visible = remember(recommendations) { recommendations != null }

  AnimatedVisibility(
      modifier = Modifier.padding(MaterialTheme.keylines.content),
      visible = visible,
  ) {
    if (recommendations != null) {
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
}

@Composable
private fun RecommendationList(
    modifier: Modifier = Modifier,
    recommendations: StockRecommendations,
) {
  LazyColumn(
      modifier = modifier,
  ) {
    items(
        items = recommendations.recommendations,
        key = { it.raw },
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
    recommendation: StockSymbol,
) {
  Box(
      modifier = modifier,
  ) {
    Text(
        text = recommendation.raw,
        style = MaterialTheme.typography.body1,
    )
  }
}

@Preview
@Composable
private fun PreviewDigRecommendations() {
  DigRecommendations(
      isLoading = false,
      recommendations = null,
      onRefresh = {},
  )
}
