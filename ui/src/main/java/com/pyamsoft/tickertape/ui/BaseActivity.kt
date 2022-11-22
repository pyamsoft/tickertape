package com.pyamsoft.tickertape.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.ui.app.PYDroidActivity
import com.pyamsoft.pydroid.ui.version.VersionUpgradeAvailable

/** Base class for Activity */
abstract class BaseActivity : PYDroidActivity() {

  /** Accessed from NewVersionWidget */
  internal var versionUpgrader: VersionUpgradeAvailable? = null

  @CheckResult
  private fun getApplicationName(): CharSequence {
    return applicationInfo.loadLabel(applicationContext.packageManager)
  }

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Create it here to be used by NewVersionWidget later on
    versionUpgrader =
        VersionUpgradeAvailable.create(
            activity = this,
            appName = getApplicationName().toString(),
        )
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()

    versionUpgrader?.destroy()
    versionUpgrader = null
  }
}
