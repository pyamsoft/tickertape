package com.pyamsoft.tickertape.ui

import android.widget.DatePicker
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import java.time.LocalDate

private data class DatePickState
internal constructor(
    val pickedDate: LocalDate,
    val onDatePicked: (Int, Int, Int) -> Unit,
)

@Composable
@CheckResult
private fun rememberDatePicker(initialDate: LocalDate): DatePickState {
  val (pickedDate, setPickedDate) = remember { mutableStateOf(initialDate) }
  val handleDatePicked by rememberUpdatedState { year: Int, month: Int, dayOfMonth: Int ->
    // Month is 0 indexed, but LocalDate expects 1 index
    val date = LocalDate.of(year, month + 1, dayOfMonth)
    setPickedDate(date)
  }

  return remember(pickedDate) {
    DatePickState(
        pickedDate = pickedDate,
        onDatePicked = handleDatePicked,
    )
  }
}

@Composable
fun DatePickerDialog(
    modifier: Modifier = Modifier,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
  val handleDateSelected by rememberUpdatedState(onDateSelected)
  val pickState = rememberDatePicker(initialDate)

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
                  pickState.onDatePicked(
                      year,
                      month,
                      dayOfMonth,
                  )
                }
              }
            },
            update = { picker ->
              pickState.onDatePicked(
                  picker.year,
                  picker.month,
                  picker.dayOfMonth,
              )
            },
        )
        Row(
            modifier = Modifier.padding(MaterialTheme.keylines.content),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Spacer(
              modifier = Modifier.weight(1F),
          )
          TextButton(
              onClick = { onDismiss() },
          ) {
            Text(
                text = "Cancel",
            )
          }

          Button(
              onClick = {
                handleDateSelected(pickState.pickedDate)
                onDismiss()
              },
          ) {
            Text(
                text = "Submit",
            )
          }
        }
      }
    }
  }
}
