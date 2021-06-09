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

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.changelog.ChangeLogActivity
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.commitNow
import com.pyamsoft.tickertape.BuildConfig
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.watchlist.WatchlistFragment

internal class MainActivity : ChangeLogActivity() {

  override val checkForUpdates = false

  override val applicationIcon = R.mipmap.ic_launcher

  override val changelog: ChangeLogBuilder = buildChangeLog {}

  override val versionName = BuildConfig.VERSION_NAME

  override val fragmentContainerId: Int
    get() = requireNotNull(rootBinding).layoutConstraint.id

  override val snackbarRoot: ViewGroup
    get() {
      return requireNotNull(rootBinding).layoutConstraint
    }

  private var rootBinding: LayoutConstraintBinding? = null
  private var stateSaver: StateSaver? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_TickerTape)
    super.onCreate(savedInstanceState)
    val binding = LayoutConstraintBinding.inflate(layoutInflater).apply { rootBinding = this }
    setContentView(binding.root)

    Injector.obtainFromApplication<TickerComponent>(this)
        .plusMainComponent()
        .create(this, this, this, binding.layoutConstraint, this)
        .inject(this)

    if (savedInstanceState == null) {
      supportFragmentManager.commitNow(this) {
        replace(fragmentContainerId, WatchlistFragment.newInstance(), WatchlistFragment.TAG)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  override fun onBackPressed() {
    onBackPressedDispatcher.also { dispatcher ->
      if (dispatcher.hasEnabledCallbacks()) {
        dispatcher.onBackPressed()
      } else {
        super.onBackPressed()
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    stateSaver?.saveState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    stateSaver = null

    rootBinding = null
  }
}
