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

package com.pyamsoft.tickertape.stocks.sources.yf

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.data.StockOptionsImpl
import com.pyamsoft.tickertape.stocks.network.NetworkOptionResponse
import com.pyamsoft.tickertape.stocks.service.OptionsService
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooOptionsSource
@Inject
internal constructor(@InternalApi private val service: OptionsService) : OptionsSource {

  companion object {

    @JvmStatic
    @CheckResult
    private fun NetworkOptionResponse.Resp.OptionChain.Option.OptionContract.asContract(
        symbol: StockSymbol
    ): StockOptionsImpl.ContractImpl {
      return StockOptionsImpl.ContractImpl(
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
    val calls = chain.calls.map { it.asContract(symbol) }
    val puts = chain.puts.map { it.asContract(symbol) }

    val localId = ZoneId.systemDefault()
    return StockOptionsImpl(
        symbol = symbol,
        expirationDates = option.expirationDates.map { parseMarketTime(it, localId) },
        strikes = option.strikes.map { it.asMoney() },
        date = parseMarketTime(chain.expirationDate, localId),
        calls = calls,
        puts = puts)
  }

  override suspend fun getOptions(
      force: Boolean,
      symbol: StockSymbol,
      date: LocalDateTime?
  ): StockOptions =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val resp =
            if (date == null) service.getOptions(symbol.symbol())
            else service.getOptions(symbol.symbol(), date.toEpochSecond(ZoneOffset.UTC))
        return@withContext parseOptionsResponse(resp)
      }
}
