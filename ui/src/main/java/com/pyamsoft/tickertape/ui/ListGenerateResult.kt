package com.pyamsoft.tickertape.ui

import androidx.annotation.CheckResult

interface ListGenerateResult<T : Any> {
  val all: List<T>
  val visible: List<T>

  companion object {

    @JvmStatic
    @CheckResult
    fun <T : Any> create(all: List<T>, visible: List<T>): ListGenerateResult<T> {
      return ListGenerateResultImpl(
          all = all,
          visible = visible,
      )
    }
  }
}

private data class ListGenerateResultImpl<T : Any>(
    override val all: List<T>,
    override val visible: List<T>,
) : ListGenerateResult<T>
