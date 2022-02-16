package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.QuoteDefaults
import com.pyamsoft.tickertape.quote.R
import com.pyamsoft.tickertape.quote.test.newTestDigViewState
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.DATE_TIME_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.ui.KarinaTsoyScreen
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import java.time.LocalDateTime
import kotlin.math.abs

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun DigChart(
    modifier: Modifier = Modifier,
    state: DigViewState,
    imageLoader: ImageLoader,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val ticker = state.ticker
  val range = state.range
  val currentDate = state.currentDate
  val currentPrice = state.currentPrice
  val openingPrice = state.openingPrice
  val equityType = state.equityType
  val isOptions = remember(equityType) { equityType == EquityType.OPTION }

  val chart = ticker.chart

  Column(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    Crossfade(
        modifier = Modifier.fillMaxWidth().heightIn(min = QuoteDefaults.CHART_HEIGHT_DP),
        targetState = chart,
    ) { c ->
      if (c != null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
          Chart(
              modifier = Modifier.fillMaxWidth().height(QuoteDefaults.CHART_HEIGHT_DP),
              chart = c,
              onScrub = onScrub,
          )
          CurrentScrub(
              modifier = Modifier.fillMaxWidth(),
              range = range,
              date = currentDate,
              price = currentPrice,
              openingPrice = openingPrice,
          )
        }
      }
    }

    Ranges(
        modifier = Modifier.fillMaxWidth(),
        range = range,
        isOptions = isOptions,
        onRangeSelected = onRangeSelected,
    )
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun CurrentScrub(
    modifier: Modifier = Modifier,
    range: StockChart.IntervalRange,
    date: LocalDateTime,
    price: StockMoneyValue?,
    openingPrice: StockMoneyValue?,
) {
  AnimatedVisibility(
      modifier = modifier,
      visible = price != null,
  ) {
    if (price != null) {
      val dateFormatter =
          remember(range) {
            if (range < StockChart.IntervalRange.THREE_MONTH) DATE_TIME_FORMATTER
            else DATE_FORMATTER
          }

      Column(
          modifier = Modifier.padding(MaterialTheme.keylines.content),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
      ) {
        Text(
            text = date.format(dateFormatter.get().requireNotNull()),
            style = MaterialTheme.typography.body1,
        )
        Row(
            modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          if (openingPrice != null) {
            Text(
                modifier = Modifier.weight(0.6F),
                text = openingPrice.asMoneyValue(),
                style = MaterialTheme.typography.body1,
            )

            CurrentPriceSpacer(
                modifier = Modifier.weight(0.05F),
            )
          }

          CurrentPriceDisplay(
              modifier = Modifier.weight(1F),
              price = price,
              openingPrice = openingPrice,
          )
        }
      }
    }
  }
}

@Composable
private fun CurrentPriceSpacer(
    modifier: Modifier = Modifier,
) {
  Spacer(
      modifier = modifier,
  )
  Icon(
      imageVector = Icons.Filled.ArrowForward,
      contentDescription = "Change",
  )
  Spacer(
      modifier = modifier,
  )
}

@Composable
private fun CurrentPriceDisplay(
    modifier: Modifier = Modifier,
    price: StockMoneyValue,
    openingPrice: StockMoneyValue?,
) {

  val diff =
      remember(price, openingPrice) {
        if (openingPrice == null) null else calculateDifferences(price, openingPrice)
      }
  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.Center,
  ) {
    Text(
        text = price.asMoneyValue(),
        style =
            MaterialTheme.typography.body1.run {
              if (diff == null) this else copy(color = diff.color)
            },
    )

    if (diff != null) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "${diff.direction.sign()}${diff.amount.asMoneyValue()}",
            style = MaterialTheme.typography.caption.copy(color = diff.color),
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            text = "(${diff.direction.sign()}${diff.percent.asPercentValue()})",
            style = MaterialTheme.typography.caption.copy(color = diff.color),
        )
      }
    }
  }
}

@CheckResult
private fun calculateDifferences(
    current: StockMoneyValue,
    openingPrice: StockMoneyValue
): ScrubDifferences {
  val rawCurrent = current.value()
  val rawOpen = openingPrice.value()

  val comparison = rawCurrent.compareTo(rawOpen)
  if (comparison == 0) {
    return ScrubDifferences.ZERO
  } else {
    val direction = if (comparison < 0) StockDirection.down() else StockDirection.up()

    // Amount doesn't matter which direction
    val rawAmount = abs(rawCurrent - rawOpen)

    // Percentage is between 0-100 not 0 and 1
    val rawPercent = (rawAmount / rawOpen) * 100

    return ScrubDifferences(
        amount = rawAmount.asMoney(),
        percent = rawPercent.asPercent(),
        direction = direction,
        color = Color(direction.color()),
    )
  }
}

private data class ScrubDifferences(
    val percent: StockPercent,
    val amount: StockMoneyValue,
    val direction: StockDirection,
    val color: Color,
) {
  companion object {

    @JvmStatic
    internal val ZERO =
        ScrubDifferences(
            percent = StockPercent.none(),
            amount = StockMoneyValue.none(),
            direction = StockDirection.none(),
            color = Color.Unspecified,
        )
  }
}

@Composable
private fun Ranges(
    modifier: Modifier = Modifier,
    range: StockChart.IntervalRange,
    isOptions: Boolean,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val allRanges =
      remember(isOptions) {
        StockChart.IntervalRange.values().filter { range ->
          return@filter if (!isOptions) true
          else {
            // Options don't support these ranges
            range != StockChart.IntervalRange.FIVE_DAY &&
                range != StockChart.IntervalRange.ONE_MONTH
          }
        }
      }

  LazyRow(
      modifier = modifier.padding(top = MaterialTheme.keylines.content),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.baseline),
  ) {
    items(
        items = allRanges,
        key = { it.name },
    ) { item ->
      if (item == range) {
        Button(
            onClick = { onRangeSelected(item) },
        ) {
          Text(
              text = item.display,
          )
        }
      } else {
        TextButton(
            onClick = { onRangeSelected(item) },
        ) {
          Text(
              text = item.display,
          )
        }
      }
    }
  }
}

@Composable
@JvmOverloads
fun ChartError(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    error: Throwable,
) {
  KarinaTsoyScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = R.drawable.chart_error,
      bottomContent = {
        Text(
            textAlign = TextAlign.Center,
            text = error.message ?: "An unexpected error occurred",
            style =
                MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.error,
                ),
        )
      },
  )
}

@Preview
@Composable
private fun PreviewDigChart() {
  Surface {
    DigChart(
        state = newTestDigViewState(),
        imageLoader = createNewTestImageLoader(),
        onScrub = {},
        onRangeSelected = {},
    )
  }
}
