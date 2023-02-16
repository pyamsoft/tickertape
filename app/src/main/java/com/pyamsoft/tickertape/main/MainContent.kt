package com.pyamsoft.tickertape.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.pyamsoft.tickertape.home.HomeEntry
import com.pyamsoft.tickertape.notification.NotificationEntry
import com.pyamsoft.tickertape.portfolio.PortfolioEntry
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.Ticker

@Composable
@OptIn(ExperimentalPagerApi::class)
internal fun MainContent(
    modifier: Modifier = Modifier,
    appName: String,
    pagerState: PagerState,
    allTabs: SnapshotStateList<TopLevelMainPage>,
    onHomeDig: (Ticker) -> Unit,
    onPortfolioDig: (PortfolioStock) -> Unit,
) {
  HorizontalPager(
      modifier = modifier,
      count = allTabs.size,
      state = pagerState,
  ) { page ->
    val screen =
        remember(
            allTabs,
            page,
        ) {
          allTabs[page]
        }

    when (screen) {
      is TopLevelMainPage.Home -> {
        HomeEntry(
            modifier = Modifier.fillMaxSize(),
            appName = appName,
            onDig = onHomeDig,
        )
      }
      is TopLevelMainPage.Portfolio -> {
        PortfolioEntry(
            modifier = Modifier.fillMaxSize(),
            onDig = onPortfolioDig,
        )
      }
      is TopLevelMainPage.Notifications -> {
        NotificationEntry(
            modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
