package dev.uncandango.kubejstweaks.kubejs.kjs71.event;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.event.KubeEvent;

import static dev.uncandango.kubejstweaks.kubejs.kjs71.component.CodecComponent.CODECS;

public class RegisterCodecEventJS implements KubeEvent {
    public <T> void registerCodec(String clazz, Codec<T> codec) {
        CODECS.put(clazz, codec);
    }
}
