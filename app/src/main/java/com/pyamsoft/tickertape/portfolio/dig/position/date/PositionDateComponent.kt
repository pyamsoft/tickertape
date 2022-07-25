package com.pyamsoft.tickertape.portfolio.dig.position.date

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.position.DbPosition
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
internal interface PositionDateComponent {

  // Name arg0 because otherwise DaggerTickerComponent is bugged dagger-2.43
  fun inject(arg0: PositionDateDialog)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance positionId: DbPosition.Id,
    ): PositionDateComponent
  }
}
