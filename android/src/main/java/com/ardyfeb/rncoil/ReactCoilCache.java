package com.ardyfeb.rncoil;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import coil.memory.MemoryCache;
import coil.size.OriginalSize;
import coil.size.PixelSize;
import coil.size.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/***
 * Hacky way to access internal data class on `MemoryCache.Key`
 */
class ReactCoilCache {
    static MemoryCache.Key keyFromMap(ReadableMap map) {
        String type = Objects.requireNonNull(map.getString("type"));

        if (type.equals("complex")) {
            Size size = null;

            if (map.hasKey("size")) {
                if (map.getType("size") == ReadableType.String) {
                    size = OriginalSize.INSTANCE;
                } else {
                    size = new PixelSize(
                        Objects.requireNonNull(map.getMap("size")).getInt("width"),
                        Objects.requireNonNull(map.getMap("size")).getInt("height")
                    );
                }
            }

            ArrayList<String> transforms = new ArrayList<>();

            if (map.hasKey("transformations")) {
                ReadableArray transformationsRaw = map.getArray("transformations");

                if (transformationsRaw != null) {
                    for (int i = 0; i < transformationsRaw.size(); i++) {
                        transforms.add(transformationsRaw.getString(i));
                    }
                }
            }

            HashMap<String, String> parameters = new HashMap<>();

            if (map.hasKey("parameters")) {
                ReadableMap params = map.getMap("parameters");
                ReadableMapKeySetIterator iterator = params.keySetIterator();

                while (iterator.hasNextKey()) {
                    String key = iterator.nextKey();
                    String value = params.getString(key);

                    parameters.put(key, value);
                }
            }

            return new MemoryCache.Key.Complex(Objects.requireNonNull(map.getString("base")), transforms, size, parameters);
        } else {
            return new MemoryCache.Key.Simple(Objects.requireNonNull(map.getString("value")));
        }
    }

    static WritableMap mapFromKey(MemoryCache.Key key) {
        WritableNativeMap builder = new WritableNativeMap();

        if (key instanceof MemoryCache.Key.Complex) {
            MemoryCache.Key.Complex complexKey = (MemoryCache.Key.Complex) key;
            Size sizeType = complexKey.getSize();

            if (sizeType instanceof PixelSize) {
                PixelSize pixelSize = (PixelSize) complexKey.getSize();
                WritableNativeMap sizeMap = new WritableNativeMap();

                sizeMap.putInt("height", pixelSize.getHeight());
                sizeMap.putInt("width", pixelSize.getWidth());

                builder.putMap("size", sizeMap);
            }

            if (sizeType instanceof  OriginalSize) {
                builder.putString("size", complexKey.getSize().toString());
            }

            if (complexKey.getTransformations().size() > 0) {
                WritableArray transformArray = new WritableNativeArray();

                for (String transform : complexKey.getTransformations()) {
                    transformArray.pushString(transform);
                }

                builder.putArray("transformations", transformArray);
            }

            if (complexKey.getParameters().size() > 0) {
                WritableNativeMap parameterMap = new WritableNativeMap();

                for (Map.Entry<String, String> params : complexKey.getParameters().entrySet()) {
                    parameterMap.putString(params.getKey(), params.getValue());
                }

                builder.putMap("parameters", parameterMap);
            }

            builder.putString("base", complexKey.getBase());
            builder.putString("type", "complex");
        } else {
            builder.putString("value", ((MemoryCache.Key.Simple) key).getValue());
            builder.putString("type", "simple");
        }

        return builder;
    }
}

