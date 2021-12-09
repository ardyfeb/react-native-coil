package com.ardyfeb.rncoil

import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType

import coil.Coil
import coil.drawable.CrossfadeDrawable
import coil.request.*
import coil.size.Scale
import coil.transform.*
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

@Suppress("UNUSED_PARAMETER")
abstract class ReactCoilManagerBase<T> : SimpleViewManager<T>() where T: ImageView {
    private lateinit var requestBuilder: ImageRequest.Builder

    private var disposable: Disposable? = null

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return mapOf<String, Any>(
            ReactCoilListener.REACT_ON_START_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_START_EVENT),
            ReactCoilListener.REACT_ON_CANCEL_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_CANCEL_EVENT),
            ReactCoilListener.REACT_ON_ERROR_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_ERROR_EVENT),
            ReactCoilListener.REACT_ON_SUCCESS_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_SUCCESS_EVENT),
        )
    }

    abstract fun getImageView(reactContext: ThemedReactContext): T

    override fun createViewInstance(reactContext: ThemedReactContext): T {
        val instance = getImageView(reactContext)

        requestBuilder = ImageRequest.Builder(reactContext)
            .target{ instance.setImageDrawable(it) }
            .listener(
                object : ReactCoilListener<T>(instance) {
                    // TODO: progress listener
                }
            )

        return instance
    }

    override fun onAfterUpdateTransaction(view: T) {
        super.onAfterUpdateTransaction(view)

        disposable = Coil.imageLoader(view.context).enqueue(
            requestBuilder.build()
        )
    }

    override fun onDropViewInstance(view: T) {
        disposable?.let { if (!it.isDisposed) it.dispose() }
    }

    @ReactProp(name = "source")
    fun setSource(view: T, source: ReadableMap?) {
        if (source != null && source.hasKey("uri")) {
            if (source.hasKey("headers") && !source.isNull("headers")) {
                requestBuilder.headers(
                    ReactCoilModule.headerFromMap(source.getMap("headers")!!)
                )
            }

            if (source.hasKey("diskCachePolicy")) {
                val diskCachePolicy = ReactCoilModule.resolveCachePolicy(
                    source.getString("diskCachePolicy")!!
                )

                requestBuilder.diskCachePolicy(diskCachePolicy)
            }

            if (source.hasKey("memoryCachePolicy")) {
                val memoryCachePolicy = ReactCoilModule.resolveCachePolicy(
                    source.getString("memoryCachePolicy")!!
                )

                requestBuilder.memoryCachePolicy(memoryCachePolicy)
            }

            if (source.hasKey("networkCachePolicy")) {
                val networkCachePolicy = ReactCoilModule.resolveCachePolicy(
                    source.getString("networkCachePolicy")!!
                )

                requestBuilder.networkCachePolicy(networkCachePolicy)
            }

            requestBuilder.data(source.getString("uri"))
        } else {
            requestBuilder.data(null)
        }
    }

    @ReactProp(name = "transform")
    fun setCoilTransform(view: T, transforms: ReadableArray) {
        val mapped: MutableList<Transformation> = mutableListOf()

        for (i in 0 until transforms.size()) {
            val transform = transforms.getMap(i)!!
            val args = transform.getArray("args")!!

            when (transform.getString("className")) {
                "blur" -> {
                    val radius = args.getDouble(0).toFloat()
                    val sampling = args.getDouble(1).toFloat()

                    mapped.add(
                        BlurTransformation(view.context, radius, sampling)
                    )
                }
                "circle" -> {
                    mapped.add(CircleCropTransformation())
                }
                "grayscale" -> {
                    mapped.add(GrayscaleTransformation())
                }
                "rounded" -> {
                    mapped.add(
                        RoundedCornersTransformation(
                            args.getDouble(0).toFloat(),
                            args.getDouble(1).toFloat(),
                            args.getDouble(2).toFloat(),
                            args.getDouble(3).toFloat()
                        )
                    )
                }
            }
        }

        requestBuilder.transformations(mapped)
    }

    @ReactProp(name = "resizeMode")
    fun setResizeMode(view: T, resizeMode: String?) {
        view.scaleType = RESIZE_MODE[resizeMode]
    }

    @ReactProp(name = "scale")
    fun setScale(view: T, scale: String?) {
        if (scale != null) {
            SCALE_TYPE[scale]?.let { requestBuilder.scale(it) }
        }
    }

    @ReactProp(name = "crossfade")
    fun setCrossfade(view: T, crossfade: Int?) {
        if (crossfade != null) {
            requestBuilder.crossfade(crossfade)
        } else {
            requestBuilder.crossfade(CrossfadeDrawable.DEFAULT_DURATION)
        }
    }

    @ReactProp(name = "size")
    fun setSize(view: T, size: ReadableArray?) {
        if (size != null) {
            requestBuilder.size(size.getInt(0), size.getInt(1))
        }
    }

    @ReactProp(name = "placeholder")
    fun setPlaceholder(view: T, base64: String?) {
        if (base64 == null) {
            requestBuilder.placeholder(null)
        } else {
            requestBuilder.placeholder(
                ReactCoilBase64.base64ToDrawable(view.context, base64)
            )
        }
    }

    @ReactProp(name = "error")
    fun setError(view: T, base64: String?) {
        if (base64 == null) {
            requestBuilder.error(null)
        } else {
            requestBuilder.error(
                ReactCoilBase64.base64ToDrawable(view.context, base64)
            )
        }
    }
    @ReactProp(name = "fallback")
    fun setFallback(view: T, base64: String?) {
        if (base64 == null) {
            requestBuilder.fallback(null)
        } else {
            requestBuilder.fallback(
                ReactCoilBase64.base64ToDrawable(view.context, base64)
            )
        }
    }

    @ReactProp(name = "memoryCacheKey")
    fun setMemoryCacheKey(view: T, key: ReadableMap?) {
        if (key != null) {
            requestBuilder.memoryCacheKey(ReactCoilCache.keyFromMap(key))
        }
    }

    @ReactProp(name = "placeholderMemoryCacheKey")
    fun setPlaceholderCacheKey(view: T, key: ReadableMap?) {
        if (key != null) {
            requestBuilder.placeholderMemoryCacheKey(ReactCoilCache.keyFromMap(key))
        } else {
            requestBuilder.placeholderMemoryCacheKey(null as String?)
        }
    }

    @ReactProp(name = "videoFrameMilis")
    fun setVideoFrameMilis(view: T, frame: Int?) {
        if (frame != null) {
            requestBuilder.videoFrameMillis(frame.toLong())
        } else {
            requestBuilder.videoFrameMillis(0)
        }
    }

    @ReactProp(name = "videoFrameMicro")
    fun setVideoFrameMicro(view: T, frame: Int?) {
        if (frame != null) {
            requestBuilder.videoFrameMicros(frame.toLong())
        } else {
            requestBuilder.videoFrameMicros(0)
        }
    }

    companion object {
        private val RESIZE_MODE = mapOf(
            "contain" to ScaleType.FIT_CENTER,
            "cover" to ScaleType.CENTER_CROP,
            "stretch" to ScaleType.FIT_XY,
            "center" to ScaleType.CENTER_INSIDE
        )

        private val SCALE_TYPE = mapOf(
            "fit" to Scale.FIT,
            "fill" to Scale.FILL
        )
    }
}