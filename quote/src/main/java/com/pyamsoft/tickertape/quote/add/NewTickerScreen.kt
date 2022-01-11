package com.pyamsoft.tickertape.quote.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
@JvmOverloads
fun NewTickerScreen(
    modifier: Modifier = Modifier,
    state: NewTickerViewState,
    onTypeSelected: (EquityType) -> Unit,
    onClose: () -> Unit,
) {
  val equityType = state.equityType
  val hasEquitySelection = remember(equityType) { equityType != null }

  Surface(
      modifier = modifier,
      shape =
          MaterialTheme.shapes.medium.copy(
              bottomStart = ZeroCornerSize,
              bottomEnd = ZeroCornerSize,
          ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      TickerAddTopBar(
          modifier = Modifier.fillMaxWidth(),
          hasEquitySelection = hasEquitySelection,
          onClose = onClose,
      )

      Crossfade(
          modifier = Modifier.fillMaxWidth(),
          targetState = equityType,
      ) { et ->
        if (et != null) {
          LookupScreen(
              modifier = Modifier.fillMaxWidth(),
              equityType = et,
          )
        } else {
          EquitySelectionScreen(
              modifier = Modifier.fillMaxWidth(),
              onTypeSelected = onTypeSelected,
          )
        }
      }
    }
  }
}

@Composable
private fun LookupScreen(
    modifier: Modifier = Modifier,
    equityType: EquityType,
) {
  Column(
      modifier = modifier.padding(16.dp),
  ) {
    Text(
        text = "Selection ${equityType.name}",
        style = MaterialTheme.typography.body1,
    )
  }
}

@Composable
private fun EquitySelectionScreen(
    modifier: Modifier = Modifier,
    onTypeSelected: (EquityType) -> Unit,
) {
  val possibleTypes = remember { EquityType.values() }

  LazyColumn(
      modifier = modifier,
  ) {
    itemsIndexed(
        items = possibleTypes,
        key = { _, item -> item.name },
    ) { index, item ->
      Column(
          modifier = Modifier.fillMaxWidth(),
      ) {
        if (index > 0) {
          Divider(
              modifier = Modifier.fillMaxWidth(),
          )
        }
        TickerAddItem(
            modifier = Modifier.fillMaxWidth(),
            type = item,
            onTypeSelected = onTypeSelected,
        )
      }
    }
  }
}

@Composable
private fun TickerAddTopBar(
    modifier: Modifier = Modifier,
    hasEquitySelection: Boolean,
    onClose: () -> Unit,
) {
  val topBarBackgroundColor: Color
  val topBarContentColor: Color
  val navigationIcon: ImageVector
  if (hasEquitySelection) {
    topBarBackgroundColor = MaterialTheme.colors.primary
    topBarContentColor = MaterialTheme.colors.onPrimary
    navigationIcon = Icons.Filled.ArrowBack
  } else {
    topBarBackgroundColor = MaterialTheme.colors.surface
    topBarContentColor = MaterialTheme.colors.onSurface
    navigationIcon = Icons.Filled.Close
  }

  TopAppBar(
      modifier = modifier,
      backgroundColor = topBarBackgroundColor,
      contentColor = topBarContentColor,
      title = {},
      navigationIcon = {
        IconButton(
            onClick = onClose,
        ) {
          Icon(
              imageVector = navigationIcon,
              contentDescription = "Close",
          )
        }
      },
  )
}

@Composable
private fun TickerAddItem(
    modifier: Modifier = Modifier,
    type: EquityType,
    onTypeSelected: (EquityType) -> Unit,
) {
  Box(
      modifier =
          modifier
              .clickable { onTypeSelected(type) }
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .heightIn(min = 48.dp),
      contentAlignment = Alignment.CenterStart,
  ) {
    Text(
        text = "Add New: ${type.display}",
        style = MaterialTheme.typography.body1,
    )
  }
}

@Composable
private fun PreviewNewTickerScreen(equityType: EquityType?) {
  NewTickerScreen(
      state = MutableNewTickerViewState().apply { this.equityType = equityType },
      onClose = {},
      onTypeSelected = {},
  )
}

@Preview
@Composable
private fun PreviewNewTickerScreenNoSelection() {
  PreviewNewTickerScreen(
      equityType = null,
  )
}

@Preview
@Composable
private fun PreviewNewTickerScreenWithSelection() {
  PreviewNewTickerScreen(
      equityType = EquityType.STOCK,
  )
}
