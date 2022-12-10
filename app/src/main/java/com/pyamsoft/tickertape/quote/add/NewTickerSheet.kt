package com.pyamsoft.tickertape.quote.add

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import java.time.LocalDate
import javax.inject.Inject

internal class NewTickerSheet : BottomSheetDialogFragment() {

  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var viewModel: NewTickerViewModeler? = null

  @CheckResult
  private fun getDestination(): TickerDestination {
    val key = KEY_DESTINATION
    return requireArguments()
        .getString(key, "")
        .also { require(it.isNotBlank()) { "Must be created with key: $key" } }
        .let { TickerDestination.valueOf(it) }
  }

  private fun NewTickerViewModeler.handleCloseClicked(equityType: EquityType?) {
    if (equityType == null) {
      dismiss()
    } else {
      this.handleClearEquityType()
    }
  }

  private fun handleOptionExpirationDate(date: LocalDate) {
    viewModel
        .requireNotNull()
        .handleOptionExpirationDate(
            scope = viewLifecycleOwner.lifecycleScope,
            date = date,
        )
  }

  private fun handleSearchResultSelected(result: SearchResult) {
    viewModel
        .requireNotNull()
        .handleSearchResultSelected(
            scope = viewLifecycleOwner.lifecycleScope,
            result = result,
        )
  }

  private fun handleSubmit() {
    val vm = viewModel.requireNotNull()
    vm.handleSubmit(
        scope = viewLifecycleOwner.lifecycleScope,
        onSubmit = { vm.handleClear() },
    )
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val act = requireActivity()

    ObjectGraph.ApplicationScope.retrieve(act)
        .plusNewTickerComponent()
        .create(getDestination())
        .inject(this)

    val vm = viewModel.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.bottom_sheet_new_ticker

      setContent {
        val state = vm.state()
        val equityType = state.equityType

        act.TickerTapeTheme(themeProvider) {
          BackHandler(
              onBack = { vm.handleCloseClicked(equityType) },
          )
          NewTickerScreen(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              onClose = { vm.handleCloseClicked(equityType) },
              onTypeSelected = { vm.handleEquityTypeSelected(it) },
              onSymbolChanged = { vm.handleSymbolChanged(it) },
              onSearchResultSelected = { handleSearchResultSelected(it) },
              onSubmit = { handleSubmit() },
              onClear = { vm.handleClear() },
              onTradeSideSelected = { vm.handleTradeSideChanged(it) },
              onResultsDismissed = { vm.handleSearchResultsDismissed() },
              onOptionTypeSlected = { vm.handleOptionType(it) },
              onStrikeSelected = { vm.handleOptionStrikePrice(it) },
              onExpirationDateSelected = { handleOptionExpirationDate(it) },
              onAfterSymbolChanged = { vm.handleAfterSymbolChanged(scope = this, symbol = it) },
          )
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.requireNotNull().restoreState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
  }

  override fun onResume() {
    super.onResume()

    // Disable dragging
    // If dragging is allowed we can scroll the ResultList down but not up.
    // Scrolling down works fine, but scrolling up dismisses the bottom sheet
    //
    // By disabling dragging, the list works again. We can close the sheet via the close button or
    // by clicking outside but not by swiping down.
    val d = dialog
    if (d is BottomSheetDialog) {
      d.behavior.isDraggable = false
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    recompose()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    viewModel = null
    theming = null
  }

  companion object {

    private val TAG = NewTickerSheet::class.java.name
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
