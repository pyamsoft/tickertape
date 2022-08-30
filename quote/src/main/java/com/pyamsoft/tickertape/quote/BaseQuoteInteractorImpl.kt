package com.pyamsoft.tickertape.quote

import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

abstract class BaseQuoteInteractorImpl
protected constructor(
    private val preferences: QuotePreferences,
) : BaseQuoteInteractor {

  final override suspend fun applyNewSort(sort: QuoteSort) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        preferences.setQuoteSort(sort)
      }

  final override suspend fun listenForSortChanges(onChange: (sort: QuoteSort) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        preferences.listenForQuoteSortChanged().collectLatest(onChange)
      }
}
