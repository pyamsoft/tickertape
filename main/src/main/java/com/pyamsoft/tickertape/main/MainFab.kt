/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.tickertape.main

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview

@Composable
@JvmOverloads
internal fun MainFab(
    modifier: Modifier = Modifier,
    fabShape: Shape,
    onClick: () -> Unit,
) {
  FloatingActionButton(
      modifier = modifier,
      shape = fabShape,
      onClick = onClick,
  ) {
    Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = "Add",
    )
  }
}

@Preview
@Composable
private fun PreviewMainFab() {
Surface {
    MainFab(
        fabShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
        onClick = {},
    )
  }
}
