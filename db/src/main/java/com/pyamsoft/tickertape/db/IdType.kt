package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult

interface IdType {

  @get:CheckResult val raw: String

  @get:CheckResult val isEmpty: Boolean
}
