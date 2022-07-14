package com.pyamsoft.tickertape.portfolio.dig.position.date

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.position.DbPosition
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
internal interface PositionDateComponent {

  fun inject(dialog: PositionDateDialog)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance positionId: DbPosition.Id,
    ): PositionDateComponent
  }
}
