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

package com.pyamsoft.tickertape.portfolio.manage.positions.add

import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsInteractor
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import timber.log.Timber

class PositionsAddViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val interactor: PositionsInteractor,
    private val thisHoldingId: DbHolding.Id,
    thisSymbol: StockSymbol,
) :
    UiSavedStateViewModel<PositionsAddViewState, PositionsAddControllerEvent>(
        savedState,
        initialState =
            PositionsAddViewState(
                symbol = thisSymbol,
                numberOfShares = StockShareValue.none(),
                pricePerShare = StockMoneyValue.none())) {

  fun handleUpdateNumberOfShares(number: StockShareValue) {
    setState { copy(numberOfShares = number) }
  }

  fun handleUpdateSharePrice(price: StockMoneyValue) {
    setState { copy(pricePerShare = price) }
  }

  fun handleCreatePosition() {
    val sharePrice = state.pricePerShare
    val shareCount = state.numberOfShares
    setState(
        stateChange = {
          copy(pricePerShare = StockMoneyValue.none(), numberOfShares = StockShareValue.none())
        },
        andThen = {
          val id = thisHoldingId
          interactor
              .createPosition(id = id, numberOfShares = shareCount, pricePerShare = sharePrice)
              .onSuccess { Timber.d("Created new position $id") }
              .onFailure { Timber.e(it, "Error creating new position $id") }
        })
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<PositionsAddViewModel> {
    override fun create(savedState: UiSavedState): PositionsAddViewModel
  }
}
