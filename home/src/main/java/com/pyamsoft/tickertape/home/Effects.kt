package com.pyamsoft.tickertape.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

@Composable
fun FirstRenderEffect(callback: CoroutineScope.() -> Unit) {
  val (firstRender, setFirstRender) = remember { mutableStateOf(true) }

  val scope = rememberCoroutineScope()

  // As long as we are blank
  SideEffect {
    if (firstRender) {
      setFirstRender(false)

      // Run only the first time
      scope.callback()
    }
  }
}
