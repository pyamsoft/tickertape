package com.pyamsoft.tickertape.quote

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class DeleteRestoreViewModeler<S : UiViewState>
protected constructor(
    override val state: S,
) : AbstractViewModeler<S>(state) {

  companion object {

    @JvmStatic
    protected fun <T : Any> handleDeleteFinal(
        recentlyDeleted: MutableStateFlow<T?>,
        onDeleted: (T) -> Unit
    ) {
      val deleted = recentlyDeleted.getAndUpdate { null }
      if (deleted != null) {
        onDeleted(deleted)
      }
    }

    @JvmStatic
    protected fun <T : Any> handleRestoreDeleted(
        scope: CoroutineScope,
        recentlyDeleted: MutableStateFlow<T?>,
        restore: suspend (T) -> ResultWrapper<DbInsert.InsertResult<T>>
    ) {
      val deleted = recentlyDeleted.getAndUpdate { null }
      if (deleted != null) {
        scope.launch(context = Dispatchers.Main) {
          restore(deleted)
              .onFailure { Timber.e(it, "Error when restoring $deleted") }
              .onSuccess { result ->
                when (result) {
                  is DbInsert.InsertResult.Insert -> Timber.d("Restored: ${result.data}")
                  is DbInsert.InsertResult.Update ->
                      Timber.d("Updated: ${result.data} from $deleted")
                  is DbInsert.InsertResult.Fail -> {
                    Timber.e(result.error, "Failed to restore: $deleted")
                    // Caught by the onFailure below
                    throw result.error
                  }
                }
              }
              .onFailure {
                Timber.e(it, "Failed to restore")
                // TODO handle restore error
              }
        }
      }
    }
  }
}
