package com.pyamsoft.tickertape.stocks

import androidx.annotation.CheckResult

interface JsonParser {

  @CheckResult fun <T : Any> toJson(data: T): String

  @CheckResult
  fun <T : Any> fromJson(
      json: String,
      type: Class<T>,
  ): T?
}

@CheckResult
inline fun <reified T : Any> JsonParser.fromJson(json: String): T? {
  return this.fromJson(json, T::class.java)
}
