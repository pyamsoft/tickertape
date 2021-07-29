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

package com.pyamsoft.tickertape.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.applyAppBarOffset
import com.pyamsoft.tickertape.ui.databinding.ListitemSpacerBinding

class SpacerViewHolder
private constructor(
    binding: ListitemSpacerBinding,
    appBarActivity: AppBarActivity,
    owner: LifecycleOwner
) : RecyclerView.ViewHolder(binding.listitemSpacer) {

  init {
    binding.listitemSpacer.apply {
      outlineProvider = null
      elevation = 0F
    }

    binding.listitemSpacerGrow.apply { post { applyAppBarOffset(appBarActivity, owner) } }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        parent: ViewGroup,
        appBarActivity: AppBarActivity,
        owner: LifecycleOwner
    ): SpacerViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = ListitemSpacerBinding.inflate(inflater, parent, false)
      return SpacerViewHolder(binding, appBarActivity, owner)
    }
  }
}
