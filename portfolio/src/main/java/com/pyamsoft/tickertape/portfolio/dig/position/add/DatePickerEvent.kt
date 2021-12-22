package com.pyamsoft.tickertape.portfolio.dig.position.add

import com.pyamsoft.tickertape.db.position.DbPosition
import java.time.Month

data class DatePickerEvent(
    val positionId: DbPosition.Id,
    val year: Int,
    val month: Month,
    val dayOfMonth: Int,
)
