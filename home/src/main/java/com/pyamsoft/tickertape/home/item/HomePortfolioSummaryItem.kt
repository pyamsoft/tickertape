/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.home.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.portfolio.item.PorfolioSummaryItem
import com.pyamsoft.tickertape.stocks.api.EquityType

@Composable
@JvmOverloads
fun HomePortfolioSummaryItem(
    modifier: Modifier = Modifier,
    portfolio: PortfolioStockList,
) {
  PorfolioSummaryItem(
      modifier = modifier,
      portfolio = portfolio,

      // Hardcode to Stock
      equityType = EquityType.STOCK,
  )
}

@Preview
@Composable
private fun PreviewHomePortfolioSummaryItem() {
  Surface {
    HomePortfolioSummaryItem(
        portfolio = PortfolioStockList.empty(),
    )
  }
}
