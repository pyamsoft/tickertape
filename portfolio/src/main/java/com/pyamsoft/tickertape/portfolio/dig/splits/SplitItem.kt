package com.pyamsoft.tickertape.portfolio.dig.splits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.test.newTestSplit
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER

@Composable
@JvmOverloads
internal fun SplitItem(
    modifier: Modifier = Modifier,
    split: DbSplit,
) {
  val preSplitShareCount = split.preSplitShareCount()
  val postSplitShareCount = split.postSplitShareCount()
  val splitDate = split.splitDate()
  val displaySplitDate =
      remember(splitDate) { splitDate.format(DATE_FORMATTER.get().requireNotNull()) }

  Card(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline).fillMaxWidth(),
    ) {
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          name = "Date",
          value = displaySplitDate,
      )
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          name = "Pre-Split Share Count",
          value = preSplitShareCount.asShareValue(),
      )
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          name = "Post-Split Share Count",
          value = postSplitShareCount.asShareValue(),
      )
    }
  }
}

@Composable
private fun Info(
    modifier: Modifier = Modifier,
    name: String,
    value: String,
    valueColor: Color = Color.Unspecified,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        text = name,
        style = MaterialTheme.typography.caption,
    )
    Text(
        modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
        color = valueColor,
        text = value,
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}

@Preview
@Composable
private fun PreviewSplitItem() {
  Surface {
    SplitItem(
        split = newTestSplit(),
    )
  }
}
