package com.ardyfeb.rncoil

import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.fetch.VideoFrameFetcher
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.util.CoilUtils
import coil.util.DebugLogger

import com.facebook.react.bridge.*
import com.facebook.react.modules.network.OkHttpClientProvider
import okhttp3.Headers

import java.lang.IllegalArgumentException

class ReactCoilModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String = REACT_CLASS

    @ReactMethod
    fun setLoaderOptions(options: ReadableMap) {
        val httpClient = OkHttpClientProvider.getOkHttpClient()
            .newBuilder()
            .cache(CoilUtils.createDefaultCache(context))
            .build()

        val loader = ImageLoader.Builder(context).apply {
            availableMemoryPercentage(0.25)
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
            memoryCachePolicy(CachePolicy.DISABLED)
            okHttpClient(httpClient)
            logger(DebugLogger())
            componentRegistry {
                add(VideoFrameUriFetcher(context))
                add(VideoFrameFileFetcher(context))

                if (Build.VERSION.SDK_INT > 28) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }

                add(SvgDecoder(context))
            }
        }

        if (options.hasKey("addLastModifiedToFileCacheKey")) {
            loader.addLastModifiedToFileCacheKey(
                options.getBoolean("addLastModifiedToFileCacheKey")
            )
        }

        if (options.hasKey("allowHardware")) {
            loader.allowHardware(options.getDynamic("allowHardware").asBoolean())
        }

        if (options.hasKey("allowRgb565")) {
            loader.allowRgb565(options.getDynamic("allowRgb565").asBoolean())
        }

        if (options.hasKey("crossfade")) {
            val crossfade = options.getDynamic("crossfade")

            when (crossfade.type) {
                ReadableType.Boolean -> loader.crossfade(crossfade.asBoolean())
                ReadableType.Number -> loader.crossfade(crossfade.asInt())
                else -> {
                    throw IllegalArgumentException(
                        "Invalid `crossfade` option, expected `number` | `boolean` received `$crossfade`"
                    )
                }
            }
        }

        if (options.hasKey("bitmapPoolingEnabled")) {
            loader.bitmapPoolingEnabled(options.getBoolean("bitmapPoolingEnabled"))
        }

        if (options.hasKey("bitmapPoolPercentage")) {
            loader.bitmapPoolPercentage(options.getDouble("bitmapPoolPercentage"))
        }

        if (options.hasKey("placeholder")) {
            loader.placeholder(
                ReactCoilBase64.base64ToDrawable(context, options.getString("placeholder")!!)
            )
        }

        if (options.hasKey("error")) {
            loader.error(
                ReactCoilBase64.base64ToDrawable(context, options.getString("error")!!)
            )
        }

        if (options.hasKey("fallback")) {
            loader.fallback(
                ReactCoilBase64.base64ToDrawable(context, options.getString("fallback")!!)
            )
        }

        if (options.hasKey("diskCachePolicy")) {
            val diskCachePolicy = resolveCachePolicy(
                options.getString("diskCachePolicy")!!
            )

            loader.diskCachePolicy(diskCachePolicy)
        }

        if (options.hasKey("memoryCachePolicy")) {
            val memoryCachePolicy = resolveCachePolicy(
                options.getString("memoryCachePolicy")!!
            )

            loader.memoryCachePolicy(memoryCachePolicy)
        }

        if (options.hasKey("networkCachePolicy")) {
            val networkCachePolicy = resolveCachePolicy(
                options.getString("networkCachePolicy")!!
            )

            loader.networkCachePolicy(networkCachePolicy)
        }

        Coil.setImageLoader(loader.build())
    }

    @ReactMethod
    fun clearAllCache() {
        val loader = Coil.imageLoader(context)

        if (loader.defaults.memoryCachePolicy != CachePolicy.DISABLED) {
            clearMemoryCache()
        }

        if (loader.defaults.diskCachePolicy != CachePolicy.DISABLED) {
            clearDiskCache()
        }
    }

    @ReactMethod
    fun prefetch(sources: ReadableArray, loadTo: String) {
        for (i in 0 until sources.size()) {
            val loader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context).data(sources.getString(i))

            when (loadTo) {
                "DISK" -> {
                    request.diskCachePolicy(CachePolicy.ENABLED)
                    request.memoryCachePolicy(CachePolicy.DISABLED)
                }
                "MEMORY" -> {
                    request.diskCachePolicy(CachePolicy.DISABLED)
                    request.memoryCachePolicy(CachePolicy.ENABLED)
                }
            }

            loader.enqueue(request.build())
        }
    }

    @ReactMethod()
    fun clearMemoryCache() {
        Coil.imageLoader(context).memoryCache.clear()
    }

    @ReactMethod()
    fun clearDiskCache() {
      	CoilUtils.createDefaultCache(context).directory.deleteRecursively()
    }

    companion object {
        private const val REACT_CLASS = "RCTCoilModule"

        fun resolveCachePolicy(str: String): CachePolicy {
            val policy = when (str) {
                "ENABLED" -> CachePolicy.ENABLED
                "WRITE_ONLY" -> CachePolicy.WRITE_ONLY
                "READ_ONLY" -> CachePolicy.READ_ONLY
                "DISABLED" -> CachePolicy.DISABLED
                else -> null
            }

            return policy ?: throw NoSuchKeyException("Unrecognized cache policy")
        }

        fun headerFromMap(map: ReadableMap): Headers {
            val builder = Headers.Builder()

            for ((key, value) in map.entryIterator) {
                builder.add(key, value as String)
            }

            return builder.build()
        }

//        fun loadAsDrawable(context: Context, uri: String, callback: (drawable: Drawable?) -> Unit) {
//            val request = ImageRequest.Builder(context)
//                .crossfade(false)
//                .data(uri)
//                .target(
//                    onSuccess = { drawable -> callback(drawable) },
//                    onError = { callback(null) }
//                )
//                .build()
//
//            Coil.imageLoader(context).enqueue(request)
//        }
    }
}
