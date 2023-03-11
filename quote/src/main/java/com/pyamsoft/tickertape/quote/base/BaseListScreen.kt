package com.pyamsoft.tickertape.quote.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.ErrorScreen
import com.pyamsoft.tickertape.ui.FabDefaults
import com.pyamsoft.tickertape.ui.renderPYDroidExtras
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import kotlinx.coroutines.CoroutineScope

@Composable
fun <T : Any> BaseListScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    isLoading: Boolean,
    pageError: Throwable?,
    list: SnapshotStateList<T>,
    search: String,
    tab: EquityType,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
    itemKey: (Int, T) -> String,
    renderHeader: (@Composable () -> Unit)? = null,
    renderEmptyState: @Composable () -> Unit,
    renderListItem: @Composable (T) -> Unit,
) {
  val scaffoldState = rememberScaffoldState()

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
  ) { pv ->
    Content(
        modifier = Modifier.fillMaxSize(),
        scaffoldPaddingValues = pv,
        imageLoader = imageLoader,
        navBarBottomHeight = navBarBottomHeight,
        onRefresh = onRefresh,
        onSearchChanged = onSearchChanged,
        onTabUpdated = onTabUpdated,
        onRegenerateList = onRegenerateList,
        itemKey = itemKey,
        renderHeader = renderHeader,
        renderEmptyState = renderEmptyState,
        renderListItem = renderListItem,
        pageError = pageError,
        list = list,
        search = search,
        tab = tab,
    )
  }
}

@Composable
private fun <T : Any> Content(
    modifier: Modifier = Modifier,
    scaffoldPaddingValues: PaddingValues,
    imageLoader: ImageLoader,
    pageError: Throwable?,
    list: List<T>,
    search: String,
    tab: EquityType,
    navBarBottomHeight: Int,
    onSearchChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
    itemKey: (Int, T) -> String,
    renderEmptyState: @Composable () -> Unit,
    renderHeader: (@Composable () -> Unit)? = null,
    renderListItem: @Composable (T) -> Unit,
) {
  val density = LocalDensity.current

  val bottomPaddingDp =
      remember(
          density,
          navBarBottomHeight,
      ) {
        density.run { navBarBottomHeight.toDp() }
      }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomCenter,
  ) {
    ListSection(
        modifier = Modifier.fillMaxSize(),
        scaffoldPaddingValues = scaffoldPaddingValues,
        imageLoader = imageLoader,
        navBarBottomHeight = bottomPaddingDp,
        renderEmptyState = renderEmptyState,
        onSearchChanged = onSearchChanged,
        onTabUpdated = onTabUpdated,
        onRefresh = onRefresh,
        onRegenerateList = onRegenerateList,
        itemKey = itemKey,
        renderHeader = renderHeader,
        renderListItem = renderListItem,
        pageError = pageError,
        list = list,
        search = search,
        tab = tab,
    )
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun <T : Any> ListSection(
    modifier: Modifier = Modifier,
    scaffoldPaddingValues: PaddingValues,
    imageLoader: ImageLoader,
    pageError: Throwable?,
    list: List<T>,
    search: String,
    tab: EquityType,
    navBarBottomHeight: Dp,
    renderEmptyState: @Composable () -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRefresh: () -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
    itemKey: (Int, T) -> String,
    renderHeader: (@Composable () -> Unit)? = null,
    renderListItem: @Composable (T) -> Unit,
) {
  val isEmptyList = remember(list) { list.isEmpty() }

  LazyColumn(
      modifier = modifier,
  ) {
    item {
      Spacer(
          modifier = Modifier.fillMaxWidth().padding(scaffoldPaddingValues).statusBarsPadding(),
      )
    }

    renderPYDroidExtras()

    if (renderHeader != null) {
      item { renderHeader() }
    }

    stickyHeader {
      Column(
          modifier =
              Modifier.fillMaxWidth()
                  .background(
                      color =
                          MaterialTheme.colors.background.copy(
                              alpha = 0.80F,
                          ),
                  )) {
            Spacer(
                modifier = Modifier.statusBarsPadding().fillMaxWidth(),
            )

            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                search = search,
                currentTab = tab,
                onSearchChanged = onSearchChanged,
                onTabUpdated = onTabUpdated,
                onRegenerateList = onRegenerateList,
            )
          }
    }

    when {
      pageError != null -> {
        item {
          ErrorState(
              modifier =
                  Modifier.fillMaxSize().padding(horizontal = MaterialTheme.keylines.baseline),
              imageLoader = imageLoader,
              error = pageError,
              onRefresh = onRefresh,
          )
        }
      }
      isEmptyList -> {
        item { renderEmptyState() }
      }
      else -> {
        itemsIndexed(
            items = list,
            key = { index, item -> itemKey(index, item) },
        ) { index, item ->
          if (index == 0) {
            Spacer(
                modifier = Modifier.height(MaterialTheme.keylines.content),
            )
          }

          renderListItem(item)

          Spacer(
              modifier = Modifier.height(MaterialTheme.keylines.content),
          )
        }
      }
    }

    item {
      Spacer(
          modifier =
              Modifier.padding(scaffoldPaddingValues)
                  .navigationBarsPadding()
                  .height(navBarBottomHeight + FabDefaults.FAB_OFFSET_DP),
      )
    }
  }
}

@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    error: Throwable,
    onRefresh: () -> Unit,
) {
  ErrorScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      bottomContent = {
        Text(
            textAlign = TextAlign.Center,
            text = error.message ?: "An unexpected error occurred",
            style =
                MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.error,
                ),
        )

        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            textAlign = TextAlign.Center,
            text = "Please try again later.",
            style = MaterialTheme.typography.body2,
        )

        Button(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            onClick = onRefresh,
        ) {
          Text(
              text = "Refresh",
          )
        }
      },
  )
}

@Preview
@Composable
private fun PreviewBaseListScreen() {
  BaseListScreen(
      modifier = Modifier.fillMaxSize(),
      imageLoader = createNewTestImageLoader(),
      onRefresh = {},
      onSearchChanged = {},
      onTabUpdated = {},
      onRegenerateList = {},
      pageError = null,
      list = remember { mutableStateListOf() },
      tab = EquityType.STOCK,
      isLoading = false,
      renderListItem = {},
      renderHeader = null,
      renderEmptyState = {},
      itemKey = { _, _ -> "" },
      search = "",
  )
}
