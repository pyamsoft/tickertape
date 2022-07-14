package com.pyamsoft.tickertape.portfolio.dig.base

import com.pyamsoft.tickertape.db.IdType
import java.time.Month

data class DateSelectedEvent<T : IdType>(
    val id: T,
    val year: Int,
    val month: Month,
    val dayOfMonth: Int,
)
