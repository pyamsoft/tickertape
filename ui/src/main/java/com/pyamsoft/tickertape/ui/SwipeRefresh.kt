package com.pyamsoft.tickertape.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * A simple component that follows the SwipeRefresh migration guide
 *
 * https://google.github.io/accompanist/swiperefresh/#migration
 */
@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SwipeRefresh(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
  // The callback will be remembered by this
  // The boolean will be remembered by this
  val state = rememberPullRefreshState(isRefreshing, onRefresh)

  Box(
      modifier = modifier.pullRefresh(state),
  ) {
    content()

    // Place this after the content so it renders above it
    PullRefreshIndicator(
        modifier = Modifier.align(Alignment.TopCenter),
        refreshing = isRefreshing,
        state = state,
    )
  }
}

@Preview
@Composable
private fun PreviewSwipeRefresh() {
  SwipeRefresh(
      isRefreshing = false,
      onRefresh = {},
  ) {}
}
