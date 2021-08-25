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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
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
) :
    BaseUiView<NotificationViewState, NotificationViewEvent, NotificationListBinding>(parent),
    NotificationAdapter.Callback {

  override val viewBinding = NotificationListBinding::inflate

  override val layoutRoot by boundView { notificationList }

  private var modelAdapter: NotificationAdapter? = null

  private val topDecoration = LinearBoundsMarginDecoration(topMargin = 0)
  private val bottomDecoration = LinearBoundsMarginDecoration(bottomMargin = 0)
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
      modelAdapter = NotificationAdapter.create(factory, owner, this)
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

      binding.notificationList.apply {
        addItemDecoration(topDecoration)
        addItemDecoration(bottomDecoration)
      }
    }

    doOnTeardown { binding.notificationList.removeAllItemDecorations() }

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
    state.mapChanged { it.topOffset }.render(viewScope) { handleTopOffset(it) }
  }

  private fun handleTopOffset(height: Int) {
    topDecoration.setMargin(top = height)
    binding.notificationList.invalidateItemDecorations()
  }

  private fun handleBottomOffset(height: Int) {
    // Need to multiply the offset and add additional spacing
    val spacing = 16.asDp(layoutRoot.context)
    bottomDecoration.setMargin(bottom = height + spacing)
    binding.notificationList.invalidateItemDecorations()
  }

  private fun setList(items: List<NotificationViewState.ListItem>) {
    val data =
        items.map { item ->
          when (item) {
            is NotificationViewState.ListItem.BigMover ->
                NotificationItemViewState.BigMover(enabled = item.enabled)
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
