package com.ardyfeb.rncoil;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import coil.memory.MemoryCache;
import coil.size.OriginalSize;
import coil.size.PixelSize;
import coil.size.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class ReactCoilCache {
    static MemoryCache.Key keyFromMap(ReadableMap map) {
        String type = map.getString("type");

        if (type.equals("complex")) {
            Size size;

            if (map.getDynamic("size").getType() == ReadableType.String) {
                size = OriginalSize.INSTANCE;
            } else {
                size = new PixelSize(map.getMap("size").getInt("width"), map.getMap("size").getInt("height"));
            }

            ReadableArray transformationsRaw = map.getArray("transformations");
            ArrayList<String> transforms = new ArrayList<>();

            for (int i = 0; i < transformationsRaw.size(); i++) {
                transforms.add(transformationsRaw.getString(i));
            }

            HashMap<String, String> parameters = new HashMap<>();

            for (Map.Entry<String, String> params : parameters.entrySet()) {
                parameters.put(params.getKey(), params.getValue());
            }

            return new MemoryCache.Key.Complex(map.getString("base"), transforms, size, parameters);
        } else {
            return new MemoryCache.Key.Simple(map.getString("value"));
        }
    }

    static ReadableMap mapFromKey(MemoryCache.Key key) {
        WritableNativeMap builder = new WritableNativeMap();

        if (key instanceof MemoryCache.Key.Complex) {
            MemoryCache.Key.Complex complexKey = (MemoryCache.Key.Complex) key;

            if (complexKey.getSize() instanceof OriginalSize) {
                builder.putString("size", complexKey.getSize().toString());
            } else if (complexKey.getSize() instanceof PixelSize) {
                PixelSize pixelSize = (PixelSize) complexKey.getSize();
                WritableNativeMap sizeMap = new WritableNativeMap();

                sizeMap.putInt("height", pixelSize.getHeight());
                sizeMap.putInt("width", pixelSize.getWidth());

                builder.putMap("size", sizeMap);
            }

            WritableArray transformArray = new WritableNativeArray();

            for (String transform : complexKey.getTransformations()) {
                transformArray.pushString(transform);
            }

            builder.putArray("transformations", transformArray);
            
            WritableNativeMap parameterMap = new WritableNativeMap();

            for (Map.Entry<String, String> params : complexKey.getParameters().entrySet()) {
                parameterMap.putString(params.getKey(), params.getValue());
            }

            builder.putMap("parameters", parameterMap);

            builder.putString("base", complexKey.getBase());
            builder.putString("type", "complex");
        } else {
            builder.putString("value", ((MemoryCache.Key.Simple) key).getValue());
            builder.putString("type", "simple");
        }

        return builder;
    }
}

