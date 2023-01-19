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

import android.app.Activity
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.ui.theme.Theming
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModeler
@Inject
internal constructor(
    override val state: MutableMainViewState,
    private val theming: Theming,
    private val mainActionSelectionBus: EventBus<MainSelectionEvent>,
) : AbstractViewModeler<MainViewState>(state) {

  fun handleSyncDarkTheme(activity: Activity) {
    val isDark = theming.isDarkTheme(activity)
    state.theme.value = if (isDark) Theming.Mode.DARK else Theming.Mode.LIGHT
  }

  fun handleMainActionSelected(scope: CoroutineScope, page: TopLevelMainPage) {
    scope.launch(context = Dispatchers.Main) {
      mainActionSelectionBus.send(MainSelectionEvent(page = page))
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_THEME) { s.theme.value.name }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(KEY_THEME)
        ?.let { it as String }
        ?.let { Theming.Mode.valueOf(it) }
        ?.also { s.theme.value = it }
  }

  override fun saveState(outState: UiSavedStateWriter) {
    state.theme.value.also { theme ->
      if (theme != Theming.Mode.SYSTEM) {
        outState.put(KEY_THEME, theme.name)
      } else {
        outState.remove(KEY_THEME)
      }
    }
  }

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    savedInstanceState.get<String>(KEY_THEME)?.also { themeName ->
      val theme = Theming.Mode.valueOf(themeName)
      state.theme.value = theme
    }
  }

  companion object {

    private const val KEY_THEME = "theme"
  }
}
