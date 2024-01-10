/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.ListItemDefaults
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
internal fun EquitySelectionScreen(
    modifier: Modifier = Modifier,
    onTypeSelected: (EquityType) -> Unit,
) {
  val possibleTypes = remember { EquityType.entries.toMutableStateList() }

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
              .padding(
                  horizontal = MaterialTheme.keylines.content,
                  vertical = MaterialTheme.keylines.baseline)
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
