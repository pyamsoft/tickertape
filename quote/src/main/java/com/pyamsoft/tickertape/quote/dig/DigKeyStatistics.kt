package com.pyamsoft.tickertape.quote.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun DigKeyStatistics(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    statistics: KeyStatistics?,
    onRefresh: () -> Unit,
) {
  val visible = remember(statistics) { statistics != null }

  AnimatedVisibility(
      modifier = Modifier.padding(MaterialTheme.keylines.content),
      visible = visible,
  ) {
    if (statistics != null) {
      SwipeRefresh(
          modifier = modifier,
          state = rememberSwipeRefreshState(isRefreshing = isLoading),
          onRefresh = onRefresh,
      ) {
        StatisticsGrid(
            modifier = Modifier.fillMaxSize(),
            statistics = statistics,
        )
      }
    }
  }
}

@Composable
private fun StatisticsGrid(
    modifier: Modifier = Modifier,
    statistics: KeyStatistics,
) {
  LazyColumn(
      modifier = modifier,
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
          content = statistics.info.marketCap.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Enterprise Value",
          content = statistics.info.enterpriseValue.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Trailing P/E",
          content = statistics.info.trailingEps.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Forward P/E",
          content = statistics.info.forwardEps.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "PEG Ratio",
          content = statistics.info.pegRatio.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Price/Book (mrq)",
          content = statistics.info.priceToBook.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Enterprise Value/Revenue",
          content = statistics.info.enterpriseValueToRevenue.fmt,
      )
    }

    item {
      StatisticsItem(
          modifier = Modifier.fillMaxWidth(),
          title = "Enterprise Value/EBITDA",
          content = statistics.info.enterpriseValueToEbitda.fmt,
      )
    }
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
      isLoading = false,
      statistics = null,
      onRefresh = {},
  )
}
