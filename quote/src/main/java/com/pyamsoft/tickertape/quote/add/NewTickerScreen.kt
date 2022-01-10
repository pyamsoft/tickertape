package com.pyamsoft.tickertape.quote.add

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
@JvmOverloads
fun NewTickerScreen(
    modifier: Modifier = Modifier,
    onTypeSelected: (EquityType) -> Unit,
    onClose: () -> Unit,
) {
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
      TopAppBar(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = MaterialTheme.colors.surface,
          elevation = 0.dp,
          contentColor = MaterialTheme.colors.onSurface,
          title = {},
          navigationIcon = {
            IconButton(
                onClick = onClose,
            ) {
              Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close",
              )
            }
          },
      )

      LazyColumn(
          modifier = Modifier.fillMaxWidth(),
      ) {
        itemsIndexed(
            items = EquityType.values(),
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
  }
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

@Preview
@Composable
private fun PreviewNewTickerScreen() {
  NewTickerScreen(
      onClose = {},
      onTypeSelected = {},
  )
}
