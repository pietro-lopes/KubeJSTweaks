package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.rhino.type.RecordTypeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Constructor;

// Latest Rhino got rid of it, needs to find a new way to still be able to create private Records
@Mixin(RecordTypeInfo.class)
public class RecordTypeInfoMixin<T> {
    @WrapOperation(method = "getConstructor", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;"))
    private Constructor<T> getPrivateConstructor(Class<T> instance, Class<?>[] parameterTypes, Operation<Constructor<T>> original){
        try {
            var constructor = instance.getDeclaredConstructor(parameterTypes);
            constructor.trySetAccessible();
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new KubeRuntimeException(e.getMessage());
        }
    }
}
