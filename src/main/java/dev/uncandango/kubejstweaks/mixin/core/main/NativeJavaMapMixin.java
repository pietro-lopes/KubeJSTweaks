package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.rhino.NativeJavaMap;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

// https://github.com/KubeJS-Mods/Rhino/issues/67
@ConditionalMixin(modId = "rhino", versionRange = "[2101.2.7-build.74,2101.2.7-build.81]")
@Mixin(NativeJavaMap.class)
public class NativeJavaMapMixin {
    @WrapOperation(method = "get(Ldev/latvian/mods/rhino/Context;Ljava/lang/String;Ldev/latvian/mods/rhino/Scriptable;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"))
    private boolean kubejstweaks$safeContains(Map instance, Object o, Operation<Boolean> original){
        try {
            return original.call(instance,o);
        } catch (Exception e) {
            return false;
        }
    }
}
