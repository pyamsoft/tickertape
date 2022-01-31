package com.pyamsoft.tickertape.stocks.okhttp

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.pyamsoft.pydroid.core.Enforcer
import javax.net.SocketFactory
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpClientLazyCallFactory(
    debug: Boolean,
) : Call.Factory {

  private val client by lazy { createOkHttpClient(debug, DelegatingSocketFactory.create()) }

  override fun newCall(request: Request): Call {
    Enforcer.assertOffMainThread()
    return client.newCall(request)
  }

  companion object {

    @JvmStatic
    @CheckResult
    internal fun createOkHttpClient(
        debug: Boolean,
        socketFactory: SocketFactory,
    ): OkHttpClient {
      Enforcer.assertOffMainThread()

      return OkHttpClient.Builder()
          .socketFactory(socketFactory)
          .apply {
            if (debug) {
              addInterceptor(
                  HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            }
          }
          .build()
    }
  }
}
