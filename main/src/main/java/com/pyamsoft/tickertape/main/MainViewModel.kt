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

import androidx.annotation.CheckResult
import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.ui.AddNew
import com.pyamsoft.tickertape.ui.BottomOffset
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val bottomOffsetBus: EventBus<BottomOffset>,
    private val addNewBus: EventBus<AddNew>,
    private val pageBus: EventConsumer<MainPage>,
    @Named("app_name") appNameRes: Int,
) :
    UiSavedStateViewModel<MainViewState, MainControllerEvent>(
        savedState,
        MainViewState(appNameRes = appNameRes, page = DEFAULT_PAGE, bottomBarHeight = 0)) {

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      pageBus.onEvent { handleSelectPage(it, force = true) }
    }
  }

  fun handleLoadDefaultPage() {
    viewModelScope.launch(context = Dispatchers.Default) {
      val page = restoreSavedState(KEY_PAGE) { DEFAULT_PAGE.asString() }.asPage()
      Timber.d("Loading initial page: $page")
      handleSelectPage(page, force = true)
    }
  }

  fun handleConsumeBottomBarHeight(height: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      setState(
          stateChange = { copy(bottomBarHeight = height) },
          andThen = { bottomOffsetBus.send(BottomOffset(it.bottomBarHeight)) })
    }
  }

  fun handleSelectPage(
      newPage: MainPage,
      force: Boolean,
  ) {
    Timber.d("Select entry: $newPage")

    // If the pages match we can just run the after, no need to set and publish
    val oldPage = state.page
    setState(
        stateChange = { copy(page = newPage) },
        andThen = { newState ->
          publishNewSelection(requireNotNull(newState.page), oldPage, force)
        })
  }

  fun handleAddNewRequest() {
    Timber.d("Add New requested!")
    viewModelScope.launch(context = Dispatchers.Default) { addNewBus.send(AddNew) }
  }

  private suspend inline fun publishNewSelection(
      newPage: MainPage,
      oldPage: MainPage?,
      force: Boolean,
  ) {
    Timber.d("Publish selection: $oldPage -> $newPage")
    putSavedState(KEY_PAGE, newPage.asString())
    publish(MainControllerEvent.PushPage(newPage, oldPage, force))
  }

  companion object {

    private val DEFAULT_PAGE = MainPage.Home

    @CheckResult
    private fun MainPage.asString(): String {
      return this::class.java.name
    }

    @CheckResult
    private fun String.asPage(): MainPage =
        when (this) {
          MainPage.WatchList::class.java.name -> MainPage.WatchList
          MainPage.Settings::class.java.name -> MainPage.Settings
          MainPage.Home::class.java.name -> MainPage.Home
          MainPage.Portfolio::class.java.name -> MainPage.Portfolio
          else -> throw IllegalStateException("Cannot convert to MainPage: $this")
        }

    private const val KEY_PAGE = "page"
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<MainViewModel> {
    override fun create(savedState: UiSavedState): MainViewModel
  }
}
