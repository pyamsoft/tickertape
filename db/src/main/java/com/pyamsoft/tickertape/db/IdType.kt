package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult

interface IdType {

  @get:CheckResult val id: String

  @get:CheckResult val isEmpty: Boolean
}
