package com.pyamsoft.tickertape.stocks.remote

import com.pyamsoft.tickertape.stocks.JsonParser
import com.squareup.moshi.Moshi
import javax.inject.Inject

internal class MoshiJsonParser
@Inject
constructor(
    private val moshi: Moshi,
) : JsonParser {

  override fun <T : Any> toJson(data: T): String {
    return moshi.adapter<T>(data::class.java).toJson(data)
  }

  override fun <T : Any> fromJson(json: String, type: Class<T>): T? {
    return moshi.adapter(type).fromJson(json)
  }
}
