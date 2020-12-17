package com.ardyfeb.rncoil

import android.os.Build
import android.widget.ImageView.ScaleType;

import coil.Coil
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.ViewSizeResolver
import coil.transform.*
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import okhttp3.Headers

import java.util.*

class ReactCoilManager : SimpleViewManager<ReactCoil>() {
    private lateinit var requestBuilder: ImageRequest.Builder

    private var disposable: Disposable? = null

    override fun getName(): String = REACT_CLASS

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return mapOf<String, Any>(
            ReactCoilListener.REACT_ON_START_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_START_EVENT),
            ReactCoilListener.REACT_ON_CANCEL_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_CANCEL_EVENT),
            ReactCoilListener.REACT_ON_ERROR_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_ERROR_EVENT),
            ReactCoilListener.REACT_ON_SUCCESS_EVENT to MapBuilder.of("registrationName", ReactCoilListener.REACT_ON_SUCCESS_EVENT),
        )
    }

    override fun createViewInstance(reactContext: ThemedReactContext): ReactCoil {
        val instance = ReactCoil(reactContext)

        requestBuilder = ImageRequest.Builder(reactContext)
            .listener(ReactCoilListener(instance))
            .target(instance)

        return instance
    }

    override fun onAfterUpdateTransaction(reactCoil: ReactCoil) {
        super.onAfterUpdateTransaction(reactCoil)

        with (reactCoil.context) {
            this@ReactCoilManager.disposable = Coil.imageLoader(this).enqueue(
                this@ReactCoilManager.requestBuilder.build()
            )
        }
    }

    override fun onDropViewInstance(reactCoil: ReactCoil) {
        this.disposable?.let {
            if (!it.isDisposed) it.dispose()
        }
    }

    @ReactProp(name = "source")
    fun setSource(reactCoil: ReactCoil, source: ReadableMap?) {
        if (source == null || !source.hasKey("uri")) {
            return
        }

        if (source.hasKey("headers") && !source.isNull("headers")) {
            val headersMap = source.getMap("headers")!!
            val builder = Headers.Builder()

            for ((key, value) in headersMap.entryIterator) {
                builder.add(key, value as String)
            }

            requestBuilder.headers(builder.build())
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
    }

    @ReactProp(name = "transform")
    fun setTransform(reactCoil: ReactCoil, transforms: ReadableArray) {
        val mapped: MutableList<Transformation> = mutableListOf()

        for (i in 0 until transforms.size()) {
            val transform = transforms.getMap(i)!!
            val args = transform.getArray("args")!!

            when (transform.getString("className")) {
                "blur" -> {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        val radius = args.getDouble(0).toFloat()
                        val sampling = args.getDouble(1).toFloat()

                        mapped.add(
                            BlurTransformation(reactCoil.context, radius, sampling)
                        )
                    }
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

                    requestBuilder.size(ViewSizeResolver(reactCoil))
                }
            }
        }

        requestBuilder.transformations(mapped)
    }

    @ReactProp(name = "resizeMode")
    fun setResizeMode(reactCoil: ReactCoil, resizeMode: String?) {
        reactCoil.scaleType = RESIZE_MODE[resizeMode]
    }

    @ReactProp(name = "scale")
    fun setScale(reactCoil: ReactCoil, scale: String?) {
        if (scale != null) {
            SCALE_TYPE[scale]?.let { requestBuilder.scale(it) }
        }
    }

    @ReactProp(name = "crossfade")
    fun setCrossfade(reactCoil: ReactCoil, crossfade: Int?) {
        if (crossfade != null) {
            requestBuilder.crossfade(crossfade)
        }
    }

    @ReactProp(name = "size")
    fun setSize(reactCoil: ReactCoil, size: ReadableArray?) {
        if (size != null) {
            requestBuilder.size(size.getInt(0), size.getInt(1))
        }
    }

    @ReactProp(name = "placeholder")
    fun setPlaceholder(reactCoil: ReactCoil, base64: String?) {
        if (base64 == null) {
            requestBuilder.placeholder(null)
        } else {
            requestBuilder.placeholder(
                ReactCoilBase64.base64ToDrawable(reactCoil.context, base64)
            )
        }
    }

    @ReactProp(name = "error")
    fun setError(reactCoil: ReactCoil, base64: String?) {
        if (base64 == null) {
            requestBuilder.error(null)
        } else {
            requestBuilder.error(
                ReactCoilBase64.base64ToDrawable(reactCoil.context, base64)
            )
        }
    }
    @ReactProp(name = "fallback")
    fun setFallback(reactCoil: ReactCoil, base64: String?) {
        if (base64 == null) {
            requestBuilder.fallback(null)
        } else {
            requestBuilder.fallback(
                ReactCoilBase64.base64ToDrawable(reactCoil.context, base64)
            )
        }
    }

    companion object {
        private const val REACT_CLASS = "RCTCoilView"

        private val RESIZE_MODE = mapOf(
            "contain" to ScaleType.FIT_CENTER,
            "cover" to ScaleType.CENTER_CROP,
            "stretch" to ScaleType.FIT_XY,
            "center" to ScaleType.CENTER_INSIDE
        )

        private val SCALE_TYPE = mapOf<String, Scale>(
            "fit" to Scale.FIT,
            "fill" to Scale.FILL
        )
    }
}