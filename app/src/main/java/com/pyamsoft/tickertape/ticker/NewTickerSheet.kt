package com.pyamsoft.tickertape.ticker

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.quote.add.NewTickerScreen
import com.pyamsoft.tickertape.quote.add.TickerDestination
import com.pyamsoft.tickertape.stocks.api.EquityType
import javax.inject.Inject
import timber.log.Timber

internal class NewTickerSheet : BottomSheetDialogFragment() {

  @JvmField @Inject internal var theming: Theming? = null

  @CheckResult
  private fun getDestination(): TickerDestination {
    val key = KEY_DESTINATION
    return requireArguments()
        .getString(key, "")
        .also { require(it.isNotBlank()) { "Must be created with key: $key" } }
        .let { TickerDestination.valueOf(it) }
  }

  private fun handleAddTypeSelected(type: EquityType) {
    val destination = getDestination()
    Timber.d("TODO: Show add flow: $type for $destination")

    // Dismiss ourselves after we show the add flow
    dismiss()
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val act = requireActivity()
    Injector.obtainFromApplication<TickerComponent>(act)
        .plusNewTickerComponent()
        .create()
        .inject(this)

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.bottom_sheet_add

      setContent {
        TickerTapeTheme(themeProvider) {
          NewTickerScreen(
              modifier = Modifier.fillMaxWidth(),
              onClose = { dismiss() },
              onTypeSelected = { handleAddTypeSelected(it) },
          )
        }
      }
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    recompose()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    theming = null
  }

  companion object {

    private const val TAG = "TickerAddDialog"
    private const val KEY_DESTINATION = "key_destination"

    @JvmStatic
    @CheckResult
    private fun newInstance(destination: TickerDestination): DialogFragment {
      return NewTickerSheet().apply {
        arguments = Bundle().apply { putString(KEY_DESTINATION, destination.name) }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        destination: TickerDestination,
    ) {
      newInstance(destination).show(activity, TAG)
    }
  }
}
