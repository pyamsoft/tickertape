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
