package com.ardyfeb.rncoil

import android.annotation.SuppressLint
import androidx.appcompat.widget.AppCompatImageView

import coil.memory.MemoryCache
import com.facebook.react.uimanager.ThemedReactContext

@SuppressLint("ViewConstructor")
class ReactCoil(reactContext: ThemedReactContext) : AppCompatImageView(reactContext) {
  var memoryCacheKey: MemoryCache.Key? = null
}