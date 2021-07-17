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

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.tickertape.ui.UiScrollingConstraintContainer
import javax.inject.Inject

class PositionsAddContainer
@Inject
internal constructor(
    parent: ViewGroup,
    nestedPriceEntry: PositionsPriceEntry,
    nestedNumberOfSharesEntry: PositionsShareCountEntry,
    nestedCommit: PositionsCommit,
) : UiScrollingConstraintContainer<PositionsAddViewState, PositionsAddViewEvent>(parent) {
  init {
    nest(nestedNumberOfSharesEntry, nestedPriceEntry, nestedCommit)

    doOnInflate {
      adoptionParent().layout {
        nestedNumberOfSharesEntry.also {
          connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
          connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
          connect(it.id(), ConstraintSet.END, nestedPriceEntry.id(), ConstraintSet.START)
          constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
          constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
          setHorizontalWeight(it.id(), 1F)
        }

        nestedPriceEntry.also {
          connect(it.id(), ConstraintSet.TOP, nestedNumberOfSharesEntry.id(), ConstraintSet.TOP)
          connect(it.id(), ConstraintSet.START, nestedNumberOfSharesEntry.id(), ConstraintSet.END)
          connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
          constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
          constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
          setHorizontalWeight(it.id(), 1F)
        }

        nestedCommit.also {
          connect(it.id(), ConstraintSet.TOP, nestedPriceEntry.id(), ConstraintSet.BOTTOM)
          connect(it.id(), ConstraintSet.END, nestedPriceEntry.id(), ConstraintSet.END)
          constrainWidth(it.id(), ConstraintSet.WRAP_CONTENT)
          constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
        }
      }
    }
  }
}
