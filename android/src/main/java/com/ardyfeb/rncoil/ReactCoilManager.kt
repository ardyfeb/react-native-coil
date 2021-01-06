package com.ardyfeb.rncoil

import com.facebook.react.uimanager.ThemedReactContext

class ReactCoilManager : ReactCoilManagerBase<ReactCoil>() {
    override fun getName(): String = REACT_CLASS

    override fun getImageView(reactContext: ThemedReactContext): ReactCoil {
        return ReactCoil(reactContext)
    }
    
    companion object {
        private const val REACT_CLASS = "RCTCoilView"
    }
}