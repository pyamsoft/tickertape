package com.pyamsoft.tickertape.quote.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
@JvmOverloads
fun TickerAddScreen(
    modifier: Modifier = Modifier,
    onTypeSelected: (EquityType) -> Unit,
) {
  LazyColumn(
      modifier = modifier,
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
private fun PreviewTickerAddScreen() {
  Surface {
    TickerAddScreen(
        onTypeSelected = {},
    )
  }
}
