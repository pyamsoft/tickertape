package com.pyamsoft.tickertape.portfolio.dig.position.add

import com.pyamsoft.pydroid.bus.EventBus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatePickerEventBus @Inject internal constructor() :
    EventBus<DatePickerEvent> by EventBus.create()
