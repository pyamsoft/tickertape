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

import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import java.time.LocalDate

@Composable
fun DatePickerDialog(
    modifier: Modifier = Modifier,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
  val handleDateSelected by rememberUpdatedState(onDateSelected)

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = onDismiss,
  ) {
    Surface(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        shape = MaterialTheme.shapes.medium,
        elevation = DialogDefaults.Elevation,
    ) {
      Column {
        AndroidView(
            factory = {
              DatePicker(it).apply {
                init(
                    initialDate.year,
                    // Month is 1indexed, but we expect 0
                    initialDate.monthValue - 1,
                    initialDate.dayOfMonth,
                ) { _, year, month, dayOfMonth ->
                  // month is 0 index but we need it to be 1 indexed
                  val date = LocalDate.of(year, month + 1, dayOfMonth)
                  handleDateSelected(date)
                }
              }
            },
        )
      }
    }
  }
}
