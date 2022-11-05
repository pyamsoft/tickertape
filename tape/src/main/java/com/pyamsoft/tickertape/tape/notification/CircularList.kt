package com.pyamsoft.tickertape.tape.notification

import androidx.annotation.CheckResult

/** Gets an index treating the list as a circular one which wraps around on both sides */
@CheckResult
private fun List<*>.safeIndex(
    index: Int,
    maxIndex: Int = this.size,
): Int {
  require(maxIndex >= 0) {
    "Cannot resolve a safe index at $index with invalid List size: $maxIndex"
  }

  return when {
    index >= maxIndex -> index % maxIndex
    index < 0 -> safeIndex(maxIndex + index, size)
    else -> index
  }
}

/**
 * Safely pulls an item from the list in a circular way
 *
 * If the index is less than 0, pull from the end of the list If the index is greater than list
 * size, wrap around and pull the modulo index
 */
@CheckResult
fun <T> List<T>.resolveItemAtCircularIndex(index: Int): T {
  return this[safeIndex(index)]
}
