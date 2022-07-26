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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.test.newTestSplit
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme

@Composable
@JvmOverloads
internal fun SplitItem(
    modifier: Modifier = Modifier,
    split: DbSplit,
) {
  val preSplitShareCount = split.preSplitShareCount
  val postSplitShareCount = split.postSplitShareCount
  val splitDate = split.splitDate
  val displaySplitDate =
      remember(splitDate) { splitDate.format(DATE_FORMATTER.get().requireNotNull()) }

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline).fillMaxWidth(),
    ) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = displaySplitDate,
          style = MaterialTheme.typography.body2,
      )

      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = postSplitShareCount.display,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
        )

        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.keylines.typography),
            text = "for",
            style = MaterialTheme.typography.body2,
        )

        Text(
            text = preSplitShareCount.display,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
        )

        Text(
            modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
            text = "split",
            style = MaterialTheme.typography.body2,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewSplitItem() {
  PreviewTickerTapeTheme {
    Surface {
      SplitItem(
          modifier = Modifier.padding(16.dp),
          split = newTestSplit(),
      )
    }
  }
}
