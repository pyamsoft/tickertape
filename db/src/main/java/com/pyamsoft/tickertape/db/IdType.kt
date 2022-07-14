package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult

interface IdType {

  @CheckResult fun isEmpty(): Boolean
}
