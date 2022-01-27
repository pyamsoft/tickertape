package com.pyamsoft.tickertape.ui.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult

/** Only use for tests/previews */
private class TestImageLoader(context: Context) : ImageLoader {

  private val context = context.applicationContext
  private val loadingDrawable by lazy(LazyThreadSafetyMode.NONE) { ColorDrawable(Color.BLACK) }
  private val successDrawable by lazy(LazyThreadSafetyMode.NONE) { ColorDrawable(Color.GREEN) }

  private val disposable =
      object : Disposable {
        override val isDisposed: Boolean = true

        @ExperimentalCoilApi override suspend fun await() {}

        override fun dispose() {}
      }

  override val bitmapPool: BitmapPool = BitmapPool(0)
  override val defaults: DefaultRequestOptions = DefaultRequestOptions()
  override val memoryCache: MemoryCache =
      object : MemoryCache {
        override val maxSize: Int = 1
        override val size: Int = 0

        override fun clear() {}

        override fun get(key: MemoryCache.Key): Bitmap? {
          return null
        }

        override fun remove(key: MemoryCache.Key): Boolean {
          return false
        }

        override fun set(key: MemoryCache.Key, bitmap: Bitmap) {}
      }

  override fun enqueue(request: ImageRequest): Disposable {
    request.apply {
      target?.onStart(placeholder = loadingDrawable)
      target?.onSuccess(result = successDrawable)
    }
    return disposable
  }

  override suspend fun execute(request: ImageRequest): ImageResult {
    return SuccessResult(
        drawable = successDrawable,
        request = request,
        metadata =
            ImageResult.Metadata(
                memoryCacheKey = MemoryCache.Key(""),
                isSampled = false,
                dataSource = DataSource.MEMORY_CACHE,
                isPlaceholderMemoryCacheKeyPresent = false,
            ),
    )
  }

  override fun newBuilder(): ImageLoader.Builder {
    return ImageLoader.Builder(context)
  }

  override fun shutdown() {}
}

/** Only use for tests/previews */
@Composable
@CheckResult
fun createNewTestImageLoader(): ImageLoader {
  val context = LocalContext.current
  return TestImageLoader(context)
}
