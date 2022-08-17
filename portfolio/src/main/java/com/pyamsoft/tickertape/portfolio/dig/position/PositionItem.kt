package com.pyamsoft.tickertape.portfolio.dig.position

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
import com.pyamsoft.tickertape.portfolio.test.newTestPosition
import com.pyamsoft.tickertape.quote.POSITION_CHANGE_LIMIT_PERCENT
import com.pyamsoft.tickertape.quote.PriceSection
import com.pyamsoft.tickertape.quote.QUOTE_CONTENT_DEFAULT_ALPHA
import com.pyamsoft.tickertape.quote.QUOTE_CONTENT_DEFAULT_COLOR
import com.pyamsoft.tickertape.quote.TickerSizes
import com.pyamsoft.tickertape.quote.rememberCardBackgroundColorForPercentChange
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme

@Composable
@JvmOverloads
internal fun PositionItem(
    modifier: Modifier = Modifier,
    position: PositionStock,
) {
  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
      backgroundColor =
          rememberCardBackgroundColorForPercentChange(
              position.gainLossPercent,
              changeLimit = POSITION_CHANGE_LIMIT_PERCENT,
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
          position = position,
      )
      CostBasis(
          modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.content),
          position = position,
      )

      CurrentPrices(
          modifier = Modifier.fillMaxWidth(),
          position = position,
      )
    }
  }
}

@Composable
private fun CurrentPrices(
    modifier: Modifier = Modifier,
    position: PositionStock,
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
        name = "Total ${if (position.isOption && position.isSell) "Premium" else "Cost"}",
        value = position.cost,
        textStyle = MaterialTheme.typography.body1,
    )

    if (position.currentValue.isNotBlank() ||
        position.displayGainLossPercent.isNotBlank() ||
        position.displayGainLossAmount.isNotBlank()) {
      Spacer(
          modifier = Modifier.height(MaterialTheme.keylines.content),
      )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd,
    ) {
      PriceSection(
          value = position.currentValue,
          valueStyle = sizes.title,
          changeAmount = position.displayGainLossAmount,
          changePercent = position.displayGainLossPercent,
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
    position: PositionStock,
) {
  val displayOriginalPrice =
      remember(position) {
        val price = position.price
        val p = if (position.isOption) (price.value * 100).asMoney() else price
        return@remember p.display
      }

  val displayAdjustedPrice =
      remember(position) {
        val price = position.priceWithSplits(position.splits)
        val p = if (position.isOption) (price.value * 100).asMoney() else price
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
    position: PositionStock,
) {
  val displayOriginalShares =
      remember(
          position,
      ) {
        val shareCount = position.shareCount
        val s = if (position.isSell) (shareCount.value * -1).asShares() else shareCount
        return@remember s.display
      }

  val displayAdjustedShares =
      remember(
          position,
      ) {
        val shareCount = position.shareCountWithSplits(position.splits)
        val s = if (position.isSell) (shareCount.value * -1).asShares() else shareCount
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
          name = "Original ${if (position.isOption) "Contracts" else "Shares"}",
          value = displayOriginalShares,
      )
    }
    Info(
        name = "${prefix}${if (position.isOption) "Contracts" else "Shares"}",
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
          position =
              PositionStock(
                  position = newTestPosition(),
                  equityType = EquityType.STOCK,
                  tradeSide = TradeSide.BUY,
                  currentPrice = 25.0.asMoney(),
                  splits = emptyList(),
              ),
      )
    }
  }
}
