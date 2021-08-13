/*
 * Copyright 2021 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.stocks.cache

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.CacheStorage
import com.pyamsoft.cachify.MemoryCacheStorage
import java.util.concurrent.TimeUnit

@CheckResult
fun <T : Any> createNewMemoryCacheStorage(): CacheStorage<T> {
  return MemoryCacheStorage.create(5, TimeUnit.MINUTES)
}

@CheckResult
fun <T : Any> createNewDiskCacheStorage(time: Long, unit: TimeUnit): CacheStorage<T> {
  return DiskCacheStorage(unit.toMillis(time))
}


private class DiskCacheStorage<T: Any> constructor(private val ttl: Long): CacheStorage<T> {

  override suspend fun clear() {
    TODO("Not yet implemented")
  }

  override suspend fun cache(data: T) {
    TODO("Not yet implemented")
  }

  override suspend fun retrieve(): T? {
    TODO("Not yet implemented")
  }

}