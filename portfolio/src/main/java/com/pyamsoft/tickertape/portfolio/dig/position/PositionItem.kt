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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.priceWithSplits
import com.pyamsoft.tickertape.db.position.shareCountWithSplits
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.test.newTestPosition
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asShares

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
    isOption: Boolean,
    isSell: Boolean,
    splits: List<DbSplit>,
    currentPrice: StockMoneyValue?
): DisplayValues {
  val price = position.priceWithSplits(splits)
  val shareCount = position.shareCountWithSplits(splits)
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
    val currentRaw = (currentPrice.value() * shareCount.value())
    val currentValue = currentRaw.asMoney()
    displayCurrent = currentValue.asMoneyValue()

    // Gain/Loss
    val gainLossValue = currentValue.value() - totalCost.value()
    val gainLossPercent = ((gainLossValue * 100) / totalCost.value()).asPercent().asPercentValue()

    val optionsScaledValue = if (isOption) (gainLossValue * 100) else gainLossValue
    val sideScaledValue = if (isSell) optionsScaledValue * -1 else optionsScaledValue
    val amount = sideScaledValue.asMoney().asMoneyValue()

    val gainLossDirection = sideScaledValue.asDirection()
    val sign = gainLossDirection.sign()

    // Final display
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
    equityType: EquityType,
    tradeSide: TradeSide,
    position: DbPosition,
    splits: List<DbSplit>,
    currentPrice: StockMoneyValue?,
) {
  val isOption = remember(equityType) { equityType == EquityType.OPTION }
  val isSell = remember(tradeSide) { tradeSide == TradeSide.SELL }

  val displayPurchaseDate =
      remember(position) { position.purchaseDate().format(DATE_FORMATTER.get().requireNotNull()) }

  val displayOriginalPrice =
      remember(
          position,
          isOption,
      ) {
        val price = position.price()
        val p = if (isOption) (price.value() * 100).asMoney() else price
        return@remember p.asMoneyValue()
      }

  val displayAdjustedPrice =
      remember(
          position,
          isOption,
          splits,
      ) {
        val price = position.priceWithSplits(splits)
        val p = if (isOption) (price.value() * 100).asMoney() else price
        return@remember p.asMoneyValue()
      }

  val displayOriginalShares =
      remember(
          position,
          isSell,
      ) {
        val shareCount = position.shareCount()
        val s = if (isSell) (shareCount.value() * -1).asShares() else shareCount
        return@remember s.asShareValue()
      }

  val displayAdjustedShares =
      remember(
          position,
          isSell,
          splits,
      ) {
        val shareCount = position.shareCountWithSplits(splits)
        val s = if (isSell) (shareCount.value() * -1).asShares() else shareCount
        return@remember s.asShareValue()
      }

  val displayValues =
      remember(
          position,
          currentPrice,
          isOption,
          isSell,
          splits,
      ) { calculateDisplayValues(position, isOption, isSell, splits, currentPrice) }

  val prefixString =
      remember(
          displayAdjustedPrice,
          displayOriginalPrice,
          displayAdjustedShares,
          displayOriginalShares,
      ) {
        if (displayAdjustedPrice != displayOriginalPrice ||
            displayAdjustedShares != displayOriginalShares) {
          // Something has undergone a split, show adjusted prices
          return@remember "Adjusted "
        } else {
          // No splits, normal prices
          return@remember ""
        }
      }

  Card(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline).fillMaxWidth(),
    ) {
      Info(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          name = "Purchase Date",
          value = displayPurchaseDate,
          textStyle = MaterialTheme.typography.body2,
      )

      Column(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
      ) {
        if (prefixString.isNotBlank()) {
          Info(
              name = "Original Cost Basis",
              value = displayOriginalPrice,
          )
        }

        Info(
            name = "${prefixString}Cost Basis",
            value = displayAdjustedPrice,
        )
      }

      Column(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
      ) {
        if (prefixString.isNotBlank()) {
          Info(
              name = "Original ${if (isOption) "Contracts" else "Shares"}",
              value = displayOriginalShares,
          )
        }

        Info(
            name = "${prefixString}${if (isOption) "Contracts" else "Shares"}",
            value = displayAdjustedShares,
        )
      }
      Row(
          modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.typography),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Info(
            modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
            name = "Total ${if (isOption && isSell) "Premium" else "Cost"}",
            value = displayValues.cost,
        )

        if (displayValues.current.isNotBlank()) {
          Info(
              modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
              name = "Current Value",
              value = displayValues.current,
              valueColor = displayValues.color,
          )
        }
      }

      if (displayValues.gainLoss.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.typography),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Info(
              modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
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
    textStyle: TextStyle = MaterialTheme.typography.caption
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        text = name,
        style = textStyle,
    )
    Text(
        modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
        color = valueColor,
        text = value,
        style = textStyle.copy(fontWeight = FontWeight.Bold),
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
        equityType = EquityType.STOCK,
        tradeSide = TradeSide.BUY,
        currentPrice = null,
        splits = emptyList(),
    )
  }
}
