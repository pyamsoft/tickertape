package com.pyamsoft.tickertape.portfolio.add.date

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.position.DbPosition
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
internal interface PositionAddDateComponent {

  fun inject(dialog: PositionAddDateDialog)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance positionId: DbPosition.Id,
    ): PositionAddDateComponent
  }
}
