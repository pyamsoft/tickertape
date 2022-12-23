package com.pyamsoft.tickertape.quote.dig.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.widget.SwipeRefresh

private const val FAB_OFFSET = 56 + 16

@Composable
fun <T : Any> BaseDigListScreen(
    modifier: Modifier = Modifier,
    label: String,
    isAddVisible: Boolean,
    items: List<T>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAddClicked: () -> Unit,
    itemKey: (T) -> String,
    renderListItem: @Composable (T) -> Unit,
) {
  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    DigList(
        modifier = Modifier.matchParentSize(),
        items = items,
        isLoading = isLoading,
        onRefresh = onRefresh,
        itemKey = itemKey,
        listItem = renderListItem,
    )

    DigAdd(
        label = label,
        isVisible = isAddVisible,
        onClick = onAddClicked,
    )
  }
}

@Composable
private fun <T : Any> DigList(
    modifier: Modifier = Modifier,
    items: List<T>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    itemKey: (T) -> String,
    listItem: @Composable (T) -> Unit,
) {
  SwipeRefresh(
      modifier = modifier,
      isRefreshing = isLoading,
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        contentPadding = PaddingValues(MaterialTheme.keylines.content),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {
      items(
          items = items,
          key = { itemKey(it) },
      ) {
        listItem(it)
      }

      item {
        Spacer(
            modifier = Modifier.height(FAB_OFFSET.dp),
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun DigAdd(
    modifier: Modifier = Modifier,
    label: String,
    isVisible: Boolean,
    onClick: () -> Unit,
) {
  AnimatedVisibility(
      visible = isVisible,
      // Normal FAB animation
      // https://stackoverflow.com/questions/71141501/cant-animate-fab-visible-in-m3-scaffold
      enter = scaleIn(),
      exit = scaleOut(),
  ) {
    Box(
        modifier = modifier.padding(MaterialTheme.keylines.content),
    ) {
      FloatingActionButton(
          backgroundColor = MaterialTheme.colors.secondary,
          contentColor = Color.White,
          onClick = onClick,
      ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = label,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewBaseDigListScreen() {
  Surface {
    BaseDigListScreen(
        modifier = Modifier.fillMaxSize(),
        label = "Test",
        isAddVisible = true,
        isLoading = false,
        items = emptyList<String>(),
        itemKey = { "" },
        onAddClicked = {},
        onRefresh = {},
    ) {}
  }
}
