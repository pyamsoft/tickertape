package com.pyamsoft.tickertape.portfolio.dig

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
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.test.newTestPosition
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER

@Composable
@JvmOverloads
internal fun PositionItem(
    modifier: Modifier = Modifier,
    position: DbPosition,
) {

  val purchaseDateFormatter = DATE_FORMATTER.get().requireNotNull()

  Card(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
      ) {
          Info(
              modifier = Modifier.padding(end = 8.dp),
              name = "Position Opened",
              value = position.purchaseDate().format(purchaseDateFormatter),
          )
          Info(
              name = "Cost Basis",
              value = position.price().asMoneyValue(),
          )
      }
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
        modifier = Modifier.padding(start = 4.dp),
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
private fun PreviewPositionItem() {
  Surface {
    PositionItem(
        position = newTestPosition(),
    )
  }
}
