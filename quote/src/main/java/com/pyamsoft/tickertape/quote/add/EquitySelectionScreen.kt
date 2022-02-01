package com.pyamsoft.tickertape.quote.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.ListItemDefaults
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
@JvmOverloads
internal fun EquitySelectionScreen(
    modifier: Modifier = Modifier,
    onTypeSelected: (EquityType) -> Unit,
) {
  val possibleTypes = remember { EquityType.values() }

  LazyColumn(
      modifier = modifier,
  ) {
    items(
        items = possibleTypes,
        key = { it.name },
    ) { item ->
      Column(
          modifier = Modifier.fillMaxWidth(),
      ) {
        Divider(
            modifier = Modifier.fillMaxWidth(),
        )
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
              .padding(horizontal = MaterialTheme.keylines.content, vertical = MaterialTheme.keylines.baseline)
              .heightIn(min = ListItemDefaults.DefaultSize),
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
private fun PreviewEquitySelectionScreen() {
  Surface {
    EquitySelectionScreen(
        onTypeSelected = {},
    )
  }
}
