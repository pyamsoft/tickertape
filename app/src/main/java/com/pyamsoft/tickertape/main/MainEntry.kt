package com.pyamsoft.tickertape.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

internal class MainInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: MainViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
private fun WatchTabSwipe(
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
) {
  // Watch for a swipe causing a page change and update accordingly
  LaunchedEffect(
      pagerState,
      allTabs,
  ) {
    snapshotFlow { pagerState.currentPage }
        .collectLatest { index ->
          val page = allTabs[index]
          Timber.d("Page swiped: $page")
        }
  }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
private fun MountHooks(
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
) {
  WatchTabSwipe(
      pagerState = pagerState,
      allTabs = allTabs,
  )
}

@Composable
@OptIn(ExperimentalPagerApi::class)
internal fun MainEntry(
    modifier: Modifier = Modifier,
    appName: String,
) {
  val component = rememberComposableInjector { MainInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  val pagerState = rememberPagerState()
  val allTabs = rememberAllTabs()
  val scope = rememberCoroutineScope()

  MountHooks(
      pagerState = pagerState,
      allTabs = allTabs,
  )
  SaveStateDisposableEffect(viewModel)

  MainScreen(
      modifier = modifier,
      appName = appName,
      state = viewModel.state,
      pagerState = pagerState,
      allTabs = allTabs,
      onActionSelected = {
        viewModel.handleMainActionSelected(
            scope = scope,
            page = it,
        )
      },
      onHomeDig = { viewModel.handleOpenDig(it) },
      onPortfolioDig = { stock ->
        val quote = stock.ticker?.quote
        val lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol else quote?.symbol
        viewModel.handleOpenDig(
            holding = stock.holding,
            lookupSymbol = lookupSymbol,
            currentPrice = quote?.currentSession?.price,
        )
      },
      onCloseDig = { viewModel.handleCloseDig() },
  )
}
