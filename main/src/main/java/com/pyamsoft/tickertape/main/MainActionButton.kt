/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun MainActionButton(
    modifier: Modifier = Modifier,
    show: Boolean,
    page: MainPage,
    onActionSelected: (MainPage) -> Unit,
) {
  val isFabVisible =
      remember(
          page,
          show,
      ) {
        show && page == MainPage.Portfolio
      }

  AnimatedVisibility(
      visible = isFabVisible,
      // Normal FAB animation
      // https://stackoverflow.com/questions/71141501/cant-animate-fab-visible-in-m3-scaffold
      enter = scaleIn(),
      exit = scaleOut(),
  ) {
    FloatingActionButton(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        onClick = { onActionSelected(page) },
    ) {
      Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = "Add",
      )
    }
  }
}
