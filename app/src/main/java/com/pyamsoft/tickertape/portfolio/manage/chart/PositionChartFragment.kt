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

package com.pyamsoft.tickertape.portfolio.manage.chart

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import com.pyamsoft.tickertape.portfolio.manage.PositionManageDialog
import com.pyamsoft.tickertape.ui.chart.BaseChartFragment

class PositionChartFragment : BaseChartFragment() {

  override fun injectComponent(root: ViewGroup) {
    PositionManageDialog.getInjector(this)
        .plusQuoteComponent()
        .create(viewLifecycleOwner)
        .plusChartComponent()
        .create(root)
        .inject(this)
  }

  companion object {

    const val TAG = "PositionChartFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return PositionChartFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
