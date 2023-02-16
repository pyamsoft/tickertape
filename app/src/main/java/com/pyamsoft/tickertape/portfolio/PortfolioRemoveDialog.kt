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

package com.pyamsoft.tickertape.portfolio

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.quote.DeleteTicker
import javax.inject.Inject

class PortfolioRemoveInjector @Inject constructor() : ComposableInjector() {

  @JvmField @Inject internal var presenter: PortfolioDeletePresenter? = null

  override fun onDispose() {
    presenter = null
  }

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ApplicationScope.retrieve(activity).inject(this)
  }
}

@Stable
internal data class PortfolioRemoveParams(
    val stock: PortfolioStock,
)

@Composable
internal fun PortfolioRemoveDialog(
    params: PortfolioRemoveParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { PortfolioRemoveInjector() }
  val presenter = rememberNotNull(component.presenter)

  val scope = rememberCoroutineScope()

  val stock = params.stock
  val holding = stock.holding

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = onDismiss,
  ) {
    DeleteTicker(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
        symbol = holding.symbol,
        onCancel = onDismiss,
        onConfirm = {
          presenter.handleRemove(
              scope = scope,
              holding = holding.id,
              onRemoved = onDismiss,
          )
        },
    )
  }
}
