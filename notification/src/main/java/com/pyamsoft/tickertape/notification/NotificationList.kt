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

package com.pyamsoft.tickertape.notification

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.notification.databinding.NotificationListBinding
import com.pyamsoft.tickertape.notification.item.NotificationAdapter
import com.pyamsoft.tickertape.notification.item.NotificationItemComponent
import com.pyamsoft.tickertape.notification.item.NotificationItemViewState
import io.cabriole.decorator.LinearBoundsMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import timber.log.Timber

class NotificationList
@Inject
internal constructor(
    parent: ViewGroup,
    factory: NotificationItemComponent.Factory,
    owner: LifecycleOwner,
    appBarActivity: AppBarActivity,
) :
    BaseUiView<NotificationViewState, NotificationViewEvent, NotificationListBinding>(parent),
    NotificationAdapter.Callback {

  override val viewBinding = NotificationListBinding::inflate

  override val layoutRoot by boundView { notificationList }

  private var modelAdapter: NotificationAdapter? = null

  private var bottomDecoration: RecyclerView.ItemDecoration? = null
  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.notificationList.setHasFixedSize(true)

      binding.notificationList.layoutManager =
          LinearLayoutManager(binding.notificationList.context).apply {
        isItemPrefetchEnabled = true
        initialPrefetchItemCount = 3
      }
    }

    doOnInflate {
      modelAdapter = NotificationAdapter.create(factory, owner, appBarActivity, this)
      binding.notificationList.adapter = modelAdapter
    }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.notificationList.layoutManager
      if (manager is LinearLayoutManager) {
        val position = manager.findFirstVisibleItemPosition()
        if (position > 0) {
          outState.put(LAST_SCROLL_POSITION, position)
          return@doOnSaveState
        }
      }

      outState.remove<Nothing>(LAST_SCROLL_POSITION)
    }

    doOnInflate {
      val margin = 16.asDp(binding.notificationList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      LinearMarginDecoration.create(margin).apply {
        binding.notificationList.addItemDecoration(this)
      }
    }

    doOnTeardown {
      binding.notificationList.removeAllItemDecorations()
      bottomDecoration = null
    }

    doOnTeardown {
      binding.notificationList.adapter = null
      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): NotificationAdapter {
    return requireNotNull(modelAdapter)
  }

  override fun onTapeUpdated(enabled: Boolean) {
    publish(NotificationViewEvent.UpdateTape(enabled))
  }

  override fun onBigMoverUpdated(enabled: Boolean) {
    publish(NotificationViewEvent.UpdateBigMover(enabled))
  }

  override fun onRender(state: UiRender<NotificationViewState>) {
    state.mapChanged { it.list }.render(viewScope) { handleList(it) }
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handleBottomOffset(height: Int) {
    // Add additional padding to the list bottom to account for the height change in MainContainer
    bottomDecoration?.also { binding.notificationList.removeItemDecoration(it) }
    bottomDecoration =
        LinearBoundsMarginDecoration(bottomMargin = (height * 1.5).toInt()).apply {
      binding.notificationList.addItemDecoration(this)
    }
  }

  private fun setList(items: List<NotificationViewState.ListItem>) {
    val data =
        items.map { item ->
          when (item) {
            is NotificationViewState.ListItem.BigMover ->
                NotificationItemViewState.BigMover(enabled = item.enabled)
            is NotificationViewState.ListItem.Spacer -> NotificationItemViewState.Spacer
            is NotificationViewState.ListItem.Tape ->
                NotificationItemViewState.Tape(enabled = item.enabled)
          }
        }

    Timber.d("Submit data list: $data")
    usingAdapter().submitList(data)

    binding.apply { notificationList.isVisible = true }
  }

  private fun clearList() {
    usingAdapter().submitList(null)

    binding.apply { notificationList.isGone = true }
  }

  private fun handleList(list: List<NotificationViewState.ListItem>) {
    if (list.isEmpty()) {
      clearList()
    } else {
      setList(list)
    }
  }

  companion object {
    private const val LAST_SCROLL_POSITION = "notification_last_scroll_position"
  }
}