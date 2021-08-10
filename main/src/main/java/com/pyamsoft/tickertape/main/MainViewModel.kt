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
import com.pyamsoft.tickertape.stocks.api.HoldingType
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
        MainViewState(
            appNameRes = appNameRes,
            page = DEFAULT_PAGE,
            bottomBarHeight = 0,
            adding = false,
        )) {

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      pageBus.onEvent { handleSelectPage(it, force = true) }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      val adding = restoreSavedState(KEY_ADDING) { false }
      setState { copy(adding = adding) }
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
        stateChange = { copy(page = newPage, adding = false) },
        andThen = { newState ->
          putSavedState(KEY_PAGE, newPage.asString())
          publishNewSelection(requireNotNull(newState.page), oldPage, force)
        })
  }

  private fun setAdding(adding: Boolean) {
    setState(
        stateChange = { copy(adding = adding) },
        andThen = { newState -> putSavedState(KEY_ADDING, newState.adding) })
  }

  fun handleAddNewRequest() {
    return when (val page = state.page) {
      is MainPage.Portfolio -> handleAddPortfolioRequest()
      is MainPage.WatchList -> handleAddWatchlistRequest()
      else -> Timber.w("Unsupported add new request page: $page")
    }
  }

  private fun handleAddPortfolioRequest() {
    setAdding(!state.adding)
  }

  private fun handleAddWatchlistRequest() {
    viewModelScope.launch(context = Dispatchers.Default) { addNewBus.send(AddNew(null)) }
  }

  fun handleOpenAdd(type: HoldingType) {
    viewModelScope.launch(context = Dispatchers.Default) {
      setState(stateChange = { copy(adding = false) }, andThen = { addNewBus.send(AddNew(type)) })
    }
  }

  private fun publishNewSelection(
      newPage: MainPage,
      oldPage: MainPage?,
      force: Boolean,
  ) {
    Timber.d("Publish selection: $oldPage -> $newPage")
    publish(MainControllerEvent.PushPage(newPage, oldPage, force))
  }

  fun handleStopAdd() {
    setAdding(false)
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
          MainPage.Notifications::class.java.name -> MainPage.Notifications
          MainPage.Home::class.java.name -> MainPage.Home
          MainPage.Portfolio::class.java.name -> MainPage.Portfolio
          else -> throw IllegalStateException("Cannot convert to MainPage: $this")
        }

    private const val KEY_PAGE = "page"
    private const val KEY_ADDING = "adding"
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<MainViewModel> {
    override fun create(savedState: UiSavedState): MainViewModel
  }
}
