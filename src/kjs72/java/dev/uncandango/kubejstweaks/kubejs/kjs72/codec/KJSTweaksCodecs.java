package dev.uncandango.kubejstweaks.kubejs.kjs72.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public interface KJSTweaksCodecs {
    Codec<Class<?>> CODEC_CLASS = Codec.STRING.comapFlatMap(str -> {
        try {
            var c = Class.forName(str);

            return DataResult.success(c);
        } catch (ClassNotFoundException e) {
            return DataResult.error(() -> "Could not find class: " + str);
        }
    }, Class::getName);

}
