package com.pyamsoft.tickertape.quote.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.ui.Borders
import com.pyamsoft.tickertape.ui.drawBorder

private const val GRID_TITLE_FRACTION = 0.3F

@Composable
fun DigKeyStatistics(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    statistics: KeyStatistics?,
    onRefresh: () -> Unit,
) {
  val visible = remember(statistics) { true || statistics != null }

  AnimatedVisibility(
      modifier = Modifier.padding(MaterialTheme.keylines.content),
      visible = visible,
  ) {
    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(isRefreshing = isLoading),
        onRefresh = onRefresh,
    ) {
      StatisticsGrid(
          modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Composable
private fun StatisticsGrid(
    modifier: Modifier = Modifier,
) {
  LazyColumn(
      modifier =
          modifier
              // Draw the bottom border to cap off the last item
              .drawBorder(Borders.BOTTOM),
  ) {
    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Test",
          content = "Test 1 Top Thing",
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Test 2",
          content = "Test 2 Top Thing",
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Test 3",
          content = "Test 3 Top Thing",
      )
    }
  }
}

@Composable
private fun StatisticsItem(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
) {
  val density = LocalDensity.current
  val borderColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier =
            Modifier.fillMaxWidth(fraction = GRID_TITLE_FRACTION)
                // Only left and top borders, right will be handled by content and bottom will be
                // handled by next item
                .drawBorder(density, borderColor, Borders.LEFT)
                .drawBorder(density, borderColor, Borders.TOP)
                .padding(MaterialTheme.keylines.baseline),
        text = title,
        style = MaterialTheme.typography.body1,
    )

    Text(
        modifier =
            Modifier.fillMaxWidth()
                // Only left, top, and right borders, bottom will be handled by next item
                .drawBorder(density, borderColor, Borders.LEFT)
                .drawBorder(density, borderColor, Borders.TOP)
                .drawBorder(density, borderColor, Borders.RIGHT)
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
      isLoading = false,
      statistics = null,
      onRefresh = {},
  )
}
