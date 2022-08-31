package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines

@CheckResult
private fun getSortTitle(sort: QuoteSort): String =
    when (sort) {
      QuoteSort.PRE_MARKET -> "Pre-Market"
      QuoteSort.REGULAR -> "Normal Market"
      QuoteSort.AFTER_HOURS -> "After Hours"
    }

@Composable
fun QuoteSortMenu(
    modifier: Modifier = Modifier,
    sort: QuoteSort,
    onSortChanged: ((QuoteSort) -> Unit)?,
) {
  val sorts = remember { QuoteSort.values() }
  val (isExpanded, setExpanded) = remember { mutableStateOf(false) }
  val hasClickHandler = remember(onSortChanged) { onSortChanged != null }

  Column(
      modifier = modifier,
  ) {
    Row(
        modifier =
            Modifier.clickable(enabled = hasClickHandler) { setExpanded(true) }
                .padding(MaterialTheme.keylines.baseline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
          imageVector = Icons.Filled.Info,
          contentDescription = "Quote Sorts",
      )

      Text(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          text = "Sorts",
          style = MaterialTheme.typography.caption,
      )
    }

    if (onSortChanged != null) {
      DropdownMenu(
          expanded = isExpanded,
          onDismissRequest = { setExpanded(false) },
      ) {
        for (s in sorts) {
          val sortTitle = remember(sort) { getSortTitle(s) }
          DropdownMenuItem(
              onClick = {
                onSortChanged(s)
                setExpanded(false)
              },
          ) {
            RadioButton(
                selected = sort == s,
                onClick = null,
            )
            Text(
                modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
                text = sortTitle,
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewSortMenu() {
  Surface {
    QuoteSortMenu(
        sort = QuoteSort.REGULAR,
        onSortChanged = {},
    )
  }
}
