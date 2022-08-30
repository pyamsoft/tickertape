package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult

fun interface NullableComparator<T : Any> {

  @CheckResult
  fun compare(o1: T, o2: T): Int?
}