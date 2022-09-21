package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol

private val BOLD_STYLE =
    SpanStyle(
        fontWeight = FontWeight.W700,
    )

@Composable
@JvmOverloads
fun DeleteTicker(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
  val text =
      remember(symbol) {
        buildAnnotatedString {
          append("Really remove ticker: ")
          withStyle(
              style = BOLD_STYLE,
          ) {
            append(symbol.raw)
          }
          append("?")
        }
      }

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
    ) {
      Text(
          text = "Are you sure?",
          style = MaterialTheme.typography.h6,
      )
      Text(
          modifier = Modifier.padding(top = MaterialTheme.keylines.content),
          text = text,
          style = MaterialTheme.typography.body1,
      )
      Text(
          modifier = Modifier.padding(top = MaterialTheme.keylines.content),
          text =
              "If you remove this ticker, you will lose all data associated with it like alerts and positions.",
          style = MaterialTheme.typography.body1,
      )
      Row(
          modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.content),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
      ) {
        TextButton(
            onClick = onCancel,
        ) {
          Text(
              text = "Cancel",
          )
        }
        TextButton(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            onClick = onConfirm,
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.error,
                ),
        ) {
          Text(
              text = "Delete",
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDeleteTicker() {
  val symbol = "MSFT".asSymbol()
  DeleteTicker(
      symbol = symbol,
      onConfirm = {},
      onCancel = {},
  )
}
