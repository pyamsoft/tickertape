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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines

@Composable
fun EmptyList(
    modifier: Modifier = Modifier,
    sections: PortfolioDigSections,
    onAddItem: () -> Unit,
) {
  val what =
      remember(sections) {
        when (sections) {
          PortfolioDigSections.POSITIONS -> "Position"
          PortfolioDigSections.SPLITS -> "Split"
          else -> throw IllegalArgumentException("Can't use $sections in empty state")
        }
      }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.Center,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = "You do not have any ${what}s",
          style =
              MaterialTheme.typography.h6.copy(
                  color =
                      MaterialTheme.colors.onBackground.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )
      Button(
          onClick = onAddItem,
      ) {
        Text(
            text = "Add new $what",
        )
      }
    }
  }
}

@Composable
fun NoHolding(
    modifier: Modifier = Modifier,
    onAddNewHolding: () -> Unit,
) {
  Box(
      modifier = modifier,
      contentAlignment = Alignment.Center,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = "This ticker is not on your Watchlist.",
          style =
              MaterialTheme.typography.h6.copy(
                  color =
                      MaterialTheme.colors.onBackground.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )
      Button(
          onClick = onAddNewHolding,
      ) {
        Text(
            text = "Add to Watchlist",
        )
      }
    }
  }
}
