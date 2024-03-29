/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.dig.options

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.rememberAsStateList
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.OptionsChainDigViewState
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.ui.test.TestClock
import java.time.LocalDate

@Composable
fun DigOptionsChain(
    modifier: Modifier = Modifier,
    state: OptionsChainDigViewState,
    onRefresh: () -> Unit,
    onSectionChanged: (StockOptions.Contract.Type) -> Unit,
    onExpirationDateChanged: (LocalDate) -> Unit,
) {
  val contentColor = LocalContentColor.current
  val allTypes = remember { StockOptions.Contract.Type.entries.toMutableStateList() }

  val section by state.optionsSection.collectAsStateWithLifecycle()

  val error by state.optionsError.collectAsStateWithLifecycle()
  val options by state.optionsChain.collectAsStateWithLifecycle()
  val expirationDate by state.optionsExpirationDate.collectAsStateWithLifecycle()

  val selectedTabIndex = section.ordinal

  Column(
      modifier = modifier,
  ) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = Color.Transparent,
        contentColor = contentColor,
        indicator = { tabPositions ->
          val tabPosition = tabPositions[selectedTabIndex]
          TabRowDefaults.Indicator(
              modifier = Modifier.tabIndicatorOffset(tabPosition),
              color = MaterialTheme.colors.secondary,
          )
        },
    ) {
      allTypes.forEach { tab ->
        OptionTab(
            current = section,
            tab = tab,
            onSectionChanged = onSectionChanged,
        )
      }
    }

    options?.let { o ->
      OptionsExpirationDropdown(
          modifier = Modifier.padding(vertical = MaterialTheme.keylines.baseline),
          value = expirationDate,
          choices = o.expirationDates.rememberAsStateList(),
          onSelect = onExpirationDateChanged,
      )
    }

    if (error == null) {
      if (options != null) {
        val contractList =
            remember(
                options,
                section,
                // Whenever this date changes, refresh the list of options
                // TODO filter by date
                expirationDate,
            ) {
              val o = options.requireNotNull()
              val contracts =
                  when (section) {
                    StockOptions.Contract.Type.CALL -> o.calls
                    StockOptions.Contract.Type.PUT -> o.puts
                  }
              return@remember contracts.toMutableStateList()
            }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(MaterialTheme.keylines.content),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
        ) {
          items(
              items = contractList,
              key = { it.strike.value },
          ) { c ->
            ContractItem(
                modifier = Modifier.fillMaxWidth(),
                contract = c,
            )
          }
        }
      }
    } else {
      val scrollState = rememberScrollState()

      Column(
          modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
      ) {
        val errorMessage =
            remember(error) { error.requireNotNull().message ?: "An unexpected error occurred" }

        Text(
            text = errorMessage,
            style =
                MaterialTheme.typography.h6.copy(
                    color = MaterialTheme.colors.error,
                ),
        )
      }
    }
  }
}

@Composable
private fun OptionsExpirationDropdown(
    modifier: Modifier = Modifier,
    value: LocalDate?,
    choices: SnapshotStateList<LocalDate>,
    onSelect: (LocalDate) -> Unit,
) {
  val dateFormatter = DATE_FORMATTER.get().requireNotNull()
  val displayValue =
      remember(value, dateFormatter) { if (value == null) "" else value.format(dateFormatter) }

  val (isDropdownOpen, setDropdownOpen) = remember { mutableStateOf(false) }

  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    OutlinedTextField(
        modifier = Modifier.clickable { setDropdownOpen(true) },
        enabled = false,
        readOnly = true,
        singleLine = true,
        value = displayValue,
        onValueChange = {
          // TODO what do
        },
    )

    DropdownMenu(
        // Dropdown must have a max height or it goes over
        modifier =
            modifier.heightIn(
                max = 160.dp,
            ),
        expanded = isDropdownOpen,
        onDismissRequest = { setDropdownOpen(false) },
        properties =
            remember {
              PopupProperties(
                  focusable = false,
                  dismissOnBackPress = false,
                  dismissOnClickOutside = true,
              )
            },
    ) {
      for (choice in choices) {
        DropdownMenuItem(
            onClick = { onSelect(choice) },
        ) {
          val displayChoice = remember(choice, dateFormatter) { choice.format(dateFormatter) }

          Text(
              modifier = Modifier.padding(MaterialTheme.keylines.baseline),
              text = displayChoice,
          )
        }
      }
    }
  }
}

@Composable
private fun OptionTab(
    tab: StockOptions.Contract.Type,
    current: StockOptions.Contract.Type,
    onSectionChanged: (StockOptions.Contract.Type) -> Unit,
) {
  val contentColor = LocalContentColor.current

  Tab(
      selected = tab == current,
      selectedContentColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium),
      unselectedContentColor = contentColor.copy(alpha = ContentAlpha.medium),
      onClick = { onSectionChanged(tab) },
  ) {
    Text(
        modifier = Modifier.padding(vertical = MaterialTheme.keylines.typography),
        text = tab.display,
    )
  }
}

@Composable
private fun ContractItem(
    modifier: Modifier = Modifier,
    contract: StockOptions.Contract,
) {
  val dateFormatter = DATE_FORMATTER.get().requireNotNull()
  val formatted =
      remember(contract.expirationDate, dateFormatter) {
        contract.expirationDate.format(dateFormatter)
      }

  val labelColor =
      MaterialTheme.colors.onBackground.copy(
          alpha = ContentAlpha.medium,
      )

  val labelStyle =
      MaterialTheme.typography.overline.copy(
          fontWeight = FontWeight.W400,
          color = labelColor,
      )

  val strike =
      remember(contract) {
        val type =
            when (contract.type) {
              StockOptions.Contract.Type.CALL -> "C"
              StockOptions.Contract.Type.PUT -> "P"
            }
        return@remember "${contract.strike.display}$type"
      }

  Column(
      modifier = modifier,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = formatted,
          style =
              MaterialTheme.typography.h6.copy(
                  fontWeight = FontWeight.W700,
                  color =
                      MaterialTheme.colors.onBackground.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )
      Text(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          text = strike,
          style =
              MaterialTheme.typography.h6.copy(
                  fontWeight = FontWeight.W700,
              ),
      )
    }

    Row(
        modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      if (contract.bid.isValid) {
        Column {
          Text(
              text = "BID",
              style = labelStyle,
          )
          Text(
              text = contract.bid.display,
              style = MaterialTheme.typography.body2,
          )
        }
      }

      if (contract.ask.isValid) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.keylines.content),
        ) {
          Text(
              text = "ASK",
              style = labelStyle,
          )
          Text(
              text = contract.ask.display,
              style = MaterialTheme.typography.body2,
          )
        }
      }

      if (contract.mid.isValid) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.keylines.content),
        ) {
          Text(
              text = "MID",
              style = labelStyle,
          )
          Text(
              text = contract.mid.display,
              style = MaterialTheme.typography.body2,
          )
        }
      }
    }

    Row(
        modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
            text = "OI",
            style = labelStyle,
        )
        Text(
            text = "${contract.openInterest}",
            style = MaterialTheme.typography.body2,
        )
      }

      if (contract.iv.isValid) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.keylines.content),
        ) {
          Text(
              text = "IV",
              style = labelStyle,
          )
          Text(
              text = contract.iv.display,
              style = MaterialTheme.typography.body2,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDigOptionsChain() {
  val symbol = TestSymbol
  val clock = TestClock

  DigOptionsChain(
      modifier = Modifier.padding(16.dp),
      state =
          object :
              MutableDigViewState(
                  symbol = symbol,
                  clock = clock,
              ) {},
      onRefresh = {},
      onSectionChanged = {},
      onExpirationDateChanged = {},
  )
}
