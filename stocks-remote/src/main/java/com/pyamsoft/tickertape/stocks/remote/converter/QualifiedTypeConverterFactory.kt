/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.stocks.remote.converter

import androidx.annotation.CheckResult
import java.lang.reflect.Type
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

// https://github.com/square/retrofit/blob/master/samples/src/main/java/com/example/retrofit/JsonAndXmlConverters.java
internal class QualifiedTypeConverterFactory
private constructor(
    private val scalar: Converter.Factory,
    private val converters: Set<Converter.Factory>,
) : Converter.Factory() {

  override fun responseBodyConverter(
      type: Type,
      annotations: Array<out Annotation>,
      retrofit: Retrofit
  ): Converter<ResponseBody, *>? {
    for (annotation in annotations) {
      when (annotation) {
        is ScalarResponse -> return scalar.responseBodyConverter(type, annotations, retrofit)
        else -> {
          // Otherwise, in any random iteration order, try the other converters until something
          // succeeds
          for (c in converters) {
            c.responseBodyConverter(type, annotations, retrofit)?.also {
              return it
            }
          }
        }
      }
    }

    return super.responseBodyConverter(type, annotations, retrofit)
  }

  override fun requestBodyConverter(
      type: Type,
      parameterAnnotations: Array<out Annotation>,
      methodAnnotations: Array<out Annotation>,
      retrofit: Retrofit
  ): Converter<*, RequestBody>? {
    for (annotation in parameterAnnotations) {
      when (annotation) {
        is ScalarResponse ->
            return scalar.requestBodyConverter(
                type, parameterAnnotations, methodAnnotations, retrofit)
        else -> {
          // Otherwise, in any random iteration order, try the other converters until something
          // succeeds
          for (c in converters) {
            c.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)?.also {
              return it
            }
          }
        }
      }
    }

    return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        scalar: Converter.Factory,
        converters: Set<Converter.Factory>,
    ): Converter.Factory {
      return QualifiedTypeConverterFactory(
          scalar = scalar,
          converters = converters,
      )
    }
  }
}
