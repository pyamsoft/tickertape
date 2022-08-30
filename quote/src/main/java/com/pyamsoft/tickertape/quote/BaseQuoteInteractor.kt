package com.pyamsoft.tickertape.quote

interface BaseQuoteInteractor {

  suspend fun applyNewSort(sort: QuoteSort)

  suspend fun listenForSortChanges(onChange: (sort: QuoteSort) -> Unit)
}
