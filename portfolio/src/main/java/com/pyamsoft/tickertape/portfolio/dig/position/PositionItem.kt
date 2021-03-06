package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.LocalContentColor
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
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.priceWithSplits
import com.pyamsoft.tickertape.db.position.shareCountWithSplits
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.test.newTestPosition
import com.pyamsoft.tickertape.quote.PriceSection
import com.pyamsoft.tickertape.quote.QUOTE_CONTENT_DEFAULT_ALPHA
import com.pyamsoft.tickertape.quote.QUOTE_CONTENT_DEFAULT_COLOR
import com.pyamsoft.tickertape.quote.TickerSizes
import com.pyamsoft.tickertape.quote.rememberCardBackgroundColorForPercentChange
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asShares
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme
import com.pyamsoft.tickertape.ui.rememberInBackground

private data class DisplayValues(
    val cost: String,
    val current: String,
    val gainLossAmount: Double,
    val gainLossPercent: Double,
    val displayGainLossAmount: String,
    val displayGainLossPercent: String,
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
  val totalCost = (price.value * shareCount.value).asMoney()
  val displayTotal = totalCost.display

  val gainLossAmount: Double
  val gainLossPercent: Double
  val displayGainLossAmount: String
  val displayGainLossPercent: String
  val displayCurrent: String
  val colorGainLoss: Color

  if (currentPrice == null) {
    gainLossAmount = 0.0
    gainLossPercent = 0.0
    displayCurrent = ""
    displayGainLossAmount = ""
    displayGainLossPercent = ""
    colorGainLoss = Color.Unspecified
  } else {
    // Current
    val currentRaw = (currentPrice.value * shareCount.value)
    val currentValue = currentRaw.asMoney()
    displayCurrent = currentValue.display

    // Gain/Loss
    val gainLossValue = currentValue.value - totalCost.value
    val gainLossPct = (gainLossValue * 100) / totalCost.value

    val optionsScaledValue = if (isOption) (gainLossValue * 100) else gainLossValue
    val sideScaledValue = if (isSell) optionsScaledValue * -1 else optionsScaledValue

    val gainLossDirection = sideScaledValue.asDirection()
    val sign = gainLossDirection.sign

    // Final display
    gainLossAmount = sideScaledValue
    gainLossPercent = gainLossPct
    displayGainLossAmount = "${sign}${sideScaledValue.asMoney().display}"
    displayGainLossPercent = "${sign}${gainLossPct.asPercent().display}"

    // Color
    colorGainLoss = Color(gainLossDirection.color)
  }

  return DisplayValues(
      cost = displayTotal,
      current = displayCurrent,
      gainLossAmount = gainLossAmount,
      gainLossPercent = gainLossPercent,
      displayGainLossAmount = displayGainLossAmount,
      displayGainLossPercent = displayGainLossPercent,
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

  val displayValues =
      rememberInBackground(position, currentPrice, isOption, isSell, splits) {
        calculateDisplayValues(position, isOption, isSell, splits, currentPrice)
      }

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
      backgroundColor =
          rememberCardBackgroundColorForPercentChange(
              displayValues?.gainLossPercent,
              // More generous change limit since positions can be big in the red or green
              changeLimit = 30.0,
          ),
      contentColor = QUOTE_CONTENT_DEFAULT_COLOR,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.baseline),
    ) {
      PurchaseDate(
          position = position,
      )
      NumberOfShares(
          modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.content),
          isOption = isOption,
          isSell = isSell,
          position = position,
          splits = splits,
      )
      CostBasis(
          modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.content),
          isOption = isOption,
          position = position,
          splits = splits,
      )

      displayValues?.also { dv ->
        CurrentPrices(
            modifier = Modifier.fillMaxWidth(),
            isOption = isOption,
            isSell = isSell,
            displayValues = dv,
        )
      }
    }
  }
}

@Composable
private fun CurrentPrices(
    modifier: Modifier = Modifier,
    isOption: Boolean,
    isSell: Boolean,
    displayValues: DisplayValues,
) {
  val typography = MaterialTheme.typography
  val contentColor = LocalContentColor.current

  val sizes =
      remember(typography, contentColor) {
        TickerSizes.price(
            typography,
            contentColor,
        )
      }

  Column(
      modifier = modifier,
  ) {
    Info(
        name = "Total ${if (isOption && isSell) "Premium" else "Cost"}",
        value = displayValues.cost,
        textStyle = MaterialTheme.typography.body1,
    )

    if (displayValues.current.isNotBlank() ||
        displayValues.displayGainLossPercent.isNotBlank() ||
        displayValues.displayGainLossAmount.isNotBlank()) {
      Spacer(
          modifier = Modifier.height(MaterialTheme.keylines.content),
      )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd,
    ) {
      PriceSection(
          value = displayValues.current,
          valueStyle = sizes.title,
          changeAmount = displayValues.displayGainLossAmount,
          changePercent = displayValues.displayGainLossPercent,
          changeStyle = sizes.description,
      )
    }
  }
}

@Composable
private fun PurchaseDate(
    modifier: Modifier = Modifier,
    position: DbPosition,
) {
  val displayPurchaseDate =
      remember(position) { position.purchaseDate.format(DATE_FORMATTER.get().requireNotNull()) }

  Info(
      modifier = modifier,
      value = displayPurchaseDate,
      textStyle =
          MaterialTheme.typography.body1.copy(
              fontWeight = FontWeight.W700,
          ),
  )
}

@Composable
private fun CostBasis(
    modifier: Modifier = Modifier,
    isOption: Boolean,
    position: DbPosition,
    splits: List<DbSplit>,
) {
  val displayOriginalPrice =
      remember(
          position,
          isOption,
      ) {
        val price = position.price
        val p = if (isOption) (price.value * 100).asMoney() else price
        return@remember p.display
      }

  val displayAdjustedPrice =
      remember(
          position,
          isOption,
          splits,
      ) {
        val price = position.priceWithSplits(splits)
        val p = if (isOption) (price.value * 100).asMoney() else price
        return@remember p.display
      }

  val prefix =
      remember(
          displayAdjustedPrice,
          displayOriginalPrice,
      ) {
        if (displayAdjustedPrice != displayOriginalPrice) {
          // Something has undergone a split, show adjusted prices
          return@remember "Adjusted "
        } else {
          // No splits, normal prices
          return@remember ""
        }
      }

  Column(
      modifier = modifier,
  ) {
    if (prefix.isNotBlank()) {
      Info(
          name = "Original Cost Basis",
          value = displayOriginalPrice,
      )
    }

    Info(
        name = "${prefix}Cost Basis",
        value = displayAdjustedPrice,
    )
  }
}

@Composable
private fun NumberOfShares(
    modifier: Modifier = Modifier,
    isOption: Boolean,
    isSell: Boolean,
    position: DbPosition,
    splits: List<DbSplit>,
) {
  val displayOriginalShares =
      remember(
          position,
          isSell,
      ) {
        val shareCount = position.shareCount
        val s = if (isSell) (shareCount.value * -1).asShares() else shareCount
        return@remember s.display
      }

  val displayAdjustedShares =
      remember(
          position,
          isSell,
          splits,
      ) {
        val shareCount = position.shareCountWithSplits(splits)
        val s = if (isSell) (shareCount.value * -1).asShares() else shareCount
        return@remember s.display
      }

  val prefix =
      remember(
          displayAdjustedShares,
          displayOriginalShares,
      ) {
        if (displayAdjustedShares != displayOriginalShares) {
          // Something has undergone a split, show adjusted prices
          return@remember "Adjusted "
        } else {
          // No splits, normal prices
          return@remember ""
        }
      }

  Column(
      modifier = modifier,
  ) {
    if (prefix.isNotBlank()) {
      Info(
          name = "Original ${if (isOption) "Contracts" else "Shares"}",
          value = displayOriginalShares,
      )
    }
    Info(
        name = "${prefix}${if (isOption) "Contracts" else "Shares"}",
        value = displayAdjustedShares,
    )
  }
}

@Composable
private fun Info(
    modifier: Modifier = Modifier,
    name: String = "",
    value: String,
    valueColor: Color = Color.Unspecified,
    textStyle: TextStyle = MaterialTheme.typography.body2,
) {

  val displayedTextStyle =
      remember(textStyle) {
        textStyle.copy(
            color =
                textStyle.color.copy(
                    alpha = QUOTE_CONTENT_DEFAULT_ALPHA,
                ),
        )
      }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    if (name.isNotBlank()) {
      Text(
          modifier = Modifier.padding(end = MaterialTheme.keylines.typography),
          text = "${name}:",
          style = displayedTextStyle,
      )
    }

    Text(
        color = valueColor,
        text = value,
        style = displayedTextStyle.copy(fontWeight = FontWeight.W700),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}

@Preview
@Composable
private fun PreviewPositionItem() {
  PreviewTickerTapeTheme {
    Surface {
      PositionItem(
          modifier = Modifier.padding(16.dp),
          position = newTestPosition(),
          equityType = EquityType.STOCK,
          tradeSide = TradeSide.BUY,
          currentPrice = 25.0.asMoney(),
          splits = emptyList(),
      )
    }
  }
}
