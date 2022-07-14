package com.pyamsoft.tickertape.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Compute the value of some data in the background and remember the result */
@Composable
fun <T : Any> rememberInBackground(
    vararg arguments: Any?,
    onCalculate: () -> T?,
): T? {
  // Generate the display values in the background
  val (value, setValue) = remember { mutableStateOf<T?>(null) }

  // Avoid firing the launched effect again on each calculate loop
  val handleCalculation by rememberUpdatedState(onCalculate)

  LaunchedEffect(
      *arguments,
      handleCalculation,
  ) {
    // Default for computation intensive task
    this.launch(context = Dispatchers.Default) {
      val result = handleCalculation()
      setValue(result)
    }
  }

  return value
}
