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

package com.pyamsoft.tickertape.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.core.isShortTermPurchase
import com.pyamsoft.tickertape.ui.test.TestClock
import java.time.LocalDate

@Composable
fun LongTermPurchaseDateTag(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.overline,
) {
  PurchaseDateTag(
      modifier = modifier,
      isShortTerm = false,
      style = style,
  )
}

@Composable
fun ShortTermPurchaseDateTag(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.overline,
) {
  PurchaseDateTag(
      modifier = modifier,
      isShortTerm = true,
      style = style,
  )
}

@Composable
fun PurchaseDateTag(
    modifier: Modifier = Modifier,
    purchaseDate: LocalDate,
    now: LocalDate,
    style: TextStyle = MaterialTheme.typography.overline,
) {
  val isShortTerm =
      remember(
          purchaseDate,
          now,
      ) {
        purchaseDate.isShortTermPurchase(now)
      }

  PurchaseDateTag(
      modifier = modifier,
      isShortTerm = isShortTerm,
      style = style,
  )
}

@Composable
private fun PurchaseDateTag(
    modifier: Modifier = Modifier,
    isShortTerm: Boolean,
    style: TextStyle,
) {
  Surface(
      modifier = modifier,
      color = if (isShortTerm) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
      contentColor =
          if (isShortTerm) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onPrimary,
      shape = MaterialTheme.shapes.small,
      elevation = ZeroElevation,
  ) {
    Text(
        modifier =
            Modifier.padding(
                horizontal = MaterialTheme.keylines.baseline,
                vertical = MaterialTheme.keylines.typography,
            ),
        text = if (isShortTerm) "Short Term" else "Long Term",
        style =
            style.copy(
                color = LocalContentColor.current,
            ),
    )
  }
}

@Preview
@Composable
private fun PreviewPurchaseDateTagShort() {
  val clock = TestClock

  PurchaseDateTag(
      purchaseDate = LocalDate.now(clock),
      now = LocalDate.now(clock),
  )
}

@Preview
@Composable
private fun PreviewPurchaseDateTagLong() {
  val clock = TestClock

  PurchaseDateTag(
      purchaseDate = LocalDate.now(clock).minusYears(1).minusDays(1),
      now = LocalDate.now(clock),
  )
}
