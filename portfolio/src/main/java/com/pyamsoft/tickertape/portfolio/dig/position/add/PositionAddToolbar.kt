package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun PositionAddToolbar(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    onClose: () -> Unit,
) {
  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape = MaterialTheme.shapes.medium.copy(
          bottomEnd = ZeroCornerSize,
          bottomStart = ZeroCornerSize,
      ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      val contentColor = LocalContentColor.current

      TopAppBar(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.Transparent,
          contentColor = contentColor,
          elevation = ZeroElevation,
          title = {
            Text(
                text = "New Position: ${symbol.symbol()}",
            )
          },
          navigationIcon = {
            IconButton(
                onClick = onClose,
            ) {
              Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close",
              )
            }
          },
      )
    }
  }
}

@Preview
@Composable
private fun PreviewPositionAddToolbar() {
  val symbol = "MSFT".asSymbol()
  PositionAddToolbar(
      symbol = symbol,
      onClose = {},
  )
}
