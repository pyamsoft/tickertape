package com.pyamsoft.tickertape.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.version.VersionUpgradeAvailable
import timber.log.Timber

@CheckResult
private fun resolveActivity(context: Context): BaseActivity {
  return when (context) {
    is Activity -> context as? BaseActivity
    is ContextWrapper -> resolveActivity(context.baseContext)
    else -> {
      Timber.w("Provided Context is not an Activity or a ContextWrapper: $context")
      null
    }
  }
      ?: throw IllegalStateException("Could not resolve BaseActivity from Context: $context")
}

@Composable
@CheckResult
private fun rememberVersionUpgrader(context: Context): VersionUpgradeAvailable {
  return remember(context) { resolveActivity(context).versionUpgrader.requireNotNull() }
}

@Composable
fun NewVersionWidget(
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val versionUpgrader = rememberVersionUpgrader(context)

  versionUpgrader.RenderVersionCheckWidget(
      modifier = modifier,
  )
}
