package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.annotation.CheckResult
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
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent

private data class DisplayValues(
    val cost: String,
    val current: String,
    val gainLoss: String,
    val titleGainLoss: String,
    val color: Color,
)

@CheckResult
private fun calculateDisplayValues(
    position: DbPosition,
    currentPrice: StockMoneyValue?
): DisplayValues {
  val price = position.price()
  val shareCount = position.shareCount()
  val totalCost = (price.value() * shareCount.value()).asMoney()
  val displayTotal = totalCost.asMoneyValue()

  val displayCurrent: String
  val displayGainLoss: String
  val titleGainLoss: String
  val colorGainLoss: Color
  if (currentPrice == null) {
    displayCurrent = ""
    displayGainLoss = ""
    titleGainLoss = ""
    colorGainLoss = Color.Unspecified
  } else {
    // Current
    val currentValue = (currentPrice.value() * shareCount.value()).asMoney()
    displayCurrent = currentValue.asMoneyValue()

    // Gain/Loss
    val gainLossValue = currentValue.value() - totalCost.value()
    val gainLossDirection = gainLossValue.asDirection()
    val gainLossPercent = ((gainLossValue * 100) / totalCost.value()).asPercent().asPercentValue()
    val amount = gainLossValue.asMoney().asMoneyValue()
    val sign = gainLossDirection.sign()
    displayGainLoss = "${sign}${amount} (${sign}${gainLossPercent})"

    // Title
    titleGainLoss =
        when {
          gainLossDirection.isUp() -> "Gain"
          gainLossDirection.isDown() -> "Loss"
          else -> "Change"
        }

    // Color
    colorGainLoss = Color(gainLossDirection.color())
  }
  return DisplayValues(
      cost = displayTotal,
      current = displayCurrent,
      gainLoss = displayGainLoss,
      titleGainLoss = titleGainLoss,
      color = colorGainLoss,
  )
}

@Composable
@JvmOverloads
internal fun PositionItem(
    modifier: Modifier = Modifier,
    position: DbPosition,
    currentPrice: StockMoneyValue?,
) {

  val purchaseDate = position.purchaseDate()
  val price = position.price()
  val shareCount = position.shareCount()

  val displayPurchaseDate =
      remember(purchaseDate) { purchaseDate.format(DATE_FORMATTER.get().requireNotNull()) }
  val displayPrice = remember(price) { price.asMoneyValue() }
  val displayShares = remember(shareCount) { shareCount.asShareValue() }
  val displayValues =
      remember(position, currentPrice) { calculateDisplayValues(position, currentPrice) }

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
            name = "Date",
            value = displayPurchaseDate,
        )
        Info(
            modifier = Modifier.padding(end = 8.dp),
            name = "Basis",
            value = displayPrice,
        )
        Info(
            modifier = Modifier.padding(end = 8.dp),
            name = "Shares",
            value = displayShares,
        )
      }
      Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Info(
            modifier = Modifier.padding(end = 8.dp),
            name = "Cost",
            value = displayValues.cost,
        )

        if (displayValues.current.isNotBlank()) {
          Info(
              modifier = Modifier.padding(end = 8.dp),
              name = "Value",
              value = displayValues.current,
              valueColor = displayValues.color,
          )
        }
      }

      if (displayValues.gainLoss.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Info(
              modifier = Modifier.padding(end = 8.dp),
              name = displayValues.titleGainLoss,
              value = displayValues.gainLoss,
              valueColor = displayValues.color,
          )
        }
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
        currentPrice = null,
    )
  }
}
