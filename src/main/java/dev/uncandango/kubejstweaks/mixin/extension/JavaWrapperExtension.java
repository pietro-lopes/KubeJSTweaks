package dev.uncandango.kubejstweaks.mixin.extension;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public interface JavaWrapperExtension {
    @Info("Cast the object to a target type, use if Rhino can't determine the parameter type due to type erasure.")
    static <T> T cast(Context cx, Class<T> targetClass, Object object) {
        return Cast.to(cx.jsToJava(object, TypeInfo.of(targetClass)));
    }
}
