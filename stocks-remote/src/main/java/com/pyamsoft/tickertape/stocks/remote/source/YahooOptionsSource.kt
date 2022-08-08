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

package com.pyamsoft.tickertape.stocks.remote.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.network.NetworkOptionResponse
import com.pyamsoft.tickertape.stocks.remote.service.OptionsService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class YahooOptionsSource
@Inject
internal constructor(@YahooApi private val service: OptionsService) : OptionsSource {

  companion object {

    private const val INVALID_OPTIONS_FORMAT = ""
    private const val MAX_STRIKE_PRICE = 100000.0
    private const val MAX_LOOP_COUNT = 10
    private val OPTIONS_EXPIRATION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd")

    @JvmStatic
    @CheckResult
    private fun <
        T : StockOptions.Contract> NetworkOptionResponse.Resp.OptionChain.Option.OptionContract
        .asContract(
        symbol: StockSymbol,
        type: StockOptions.Contract.Type,
    ): T {
      return StockOptions.Contract.create(
          type = type,
          symbol = symbol,
          contractSymbol = this.contractSymbol.asSymbol(),
          strike = this.strike.asMoney(),
          change = this.change.asMoney(),
          percent = this.percentChange.asPercent(),
          lastPrice = this.lastPrice.asMoney(),
          bid = this.bid.asMoney(),
          ask = this.ask.asMoney(),
          iv = this.impliedVolatility.asPercent(),
          itm = this.inTheMoney,
      )
    }
  }

  @CheckResult
  private fun parseOptionsResponse(resp: NetworkOptionResponse): StockOptions {
    val option = resp.optionChain.result.first()
    val chain = option.options.first()
    val symbol = option.underlyingSymbol.asSymbol()
    val calls =
        chain.calls.map {
          it.asContract<StockOptions.Call>(
              symbol = symbol,
              type = StockOptions.Contract.Type.CALL,
          )
        }
    val puts =
        chain.puts.map {
          it.asContract<StockOptions.Put>(
              symbol = symbol,
              type = StockOptions.Contract.Type.PUT,
          )
        }

    val localId = ZoneId.systemDefault()
    return StockOptions.create(
        symbol = symbol,
        expirationDates = option.expirationDates.map { parseUTCTime(it, localId) },
        strikes = option.strikes.map { it.asMoney() },
        date = parseMarketTime(chain.expirationDate, localId),
        calls = calls,
        puts = puts,
    )
  }

  @CheckResult
  private suspend fun fetchOption(symbol: StockSymbol): StockOptions {
    val resp = service.getOptions(symbol.raw)
    return parseOptionsResponse(resp)
  }

  override suspend fun getOptions(
      force: Boolean,
      symbols: List<StockSymbol>,
  ): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val jobs =
            mutableListOf<Deferred<StockOptions>>().apply {
              for (symbol in symbols) {
                add(async { fetchOption(symbol) })
              }
            }
        return@withContext jobs.awaitAll()
      }

  override suspend fun resolveOptionLookupIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDate,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type
  ): String =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        // MSFT220114P00305000
        // MSFT 22-01-14 P $00305.000

        val dateString = expirationDate.format(OPTIONS_EXPIRATION_DATE_FORMATTER)
        val contract = if (contractType == StockOptions.Contract.Type.CALL) "C" else "P"

        // Remove the dollar sign from the strike price formatting
        val fixedStrike = strikePrice.display.replace("$", "")

        // Options IDs can only have 3 "decimal" places, figure out how many we have currently
        // and add more zeroes if we need more
        val numbersAfterDecimal = fixedStrike.substringAfter(".").length
        val neededZeroCount = 3 - numbersAfterDecimal

        // Replace the . in the price
        val zeroString = if (neededZeroCount == 0) "" else "0".repeat(neededZeroCount)
        var strikeString = "${fixedStrike.replace("." , "")}${zeroString}"

        // Here we go, time to assemble
        var tempStrike = strikePrice.value

        // The highest number YF can support is 100,000 for a strike
        tempStrike *= 10

        // A safe guard to avoid infinite loops
        var loopCount = 0
        while (tempStrike.compareTo(MAX_STRIKE_PRICE) < 0) {
          if (loopCount++ > MAX_LOOP_COUNT) {
            Timber.w("Options loop too large!. $tempStrike (Strike: $MAX_STRIKE_PRICE)")
            return@withContext INVALID_OPTIONS_FORMAT
          }
          // Pad the string with 0 until it fills to the maximum strike price
          strikeString = "0${strikeString}"
          tempStrike *= 10
        }

        return@withContext "${symbol.raw}${dateString}${contract}${strikeString}"
      }
}
