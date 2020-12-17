package com.ardyfeb.rncoil

import coil.request.ImageRequest
import coil.request.ImageResult
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter

class ReactCoilListener(private val reactCoil: ReactCoil) : ImageRequest.Listener {
    private var eventEmitter: RCTEventEmitter = (reactCoil.context as ThemedReactContext).getJSModule(
        RCTEventEmitter::class.java
    )

    override fun onCancel(request: ImageRequest) {
        eventEmitter.receiveEvent(reactCoil.id, REACT_ON_CANCEL_EVENT, null)
    }

    override fun onError(request: ImageRequest, throwable: Throwable) {
        eventEmitter.receiveEvent(reactCoil.id, REACT_ON_ERROR_EVENT, null)
    }

    override fun onStart(request: ImageRequest) {
        eventEmitter.receiveEvent(reactCoil.id, REACT_ON_START_EVENT, null)
    }

    override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
        val payload = WritableNativeMap().apply {
            putBoolean("isSampled", metadata.isSampled)
            putString("dataSource", metadata.dataSource.toString())
            putBoolean("cachedInMemory", metadata.memoryCacheKey != null)
            putBoolean("isPlaceholderMemoryCacheKeyPresent", metadata.isPlaceholderMemoryCacheKeyPresent)
        }

        eventEmitter.receiveEvent(reactCoil.id, REACT_ON_SUCCESS_EVENT, payload)
    }

    companion object {
        const val REACT_ON_START_EVENT = "onCoilStart"
        const val REACT_ON_ERROR_EVENT = "onCoilError"
        const val REACT_ON_SUCCESS_EVENT = "onCoilSuccess"
        const val REACT_ON_CANCEL_EVENT = "onCoilCancel"
    }
}