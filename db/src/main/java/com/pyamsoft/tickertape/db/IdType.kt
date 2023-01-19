package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable

@Stable
interface IdType {

  @get:CheckResult val raw: String

  @get:CheckResult val isEmpty: Boolean
}
