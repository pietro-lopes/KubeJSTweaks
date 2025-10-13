package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RegistryAccessContainer.class)
public class RegistryAccessContainerMixin {
    @WrapOperation(method = "decodeJson(Lcom/mojang/serialization/Codec;Lcom/google/gson/JsonElement;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;decode(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private DataResult kjstweaks$throwProperly(Codec instance, DynamicOps dynamicOps, Object o, Operation<DataResult> original){
        var result = original.call(instance, dynamicOps, o);
        result.getOrThrow();
        return result;
    }
}
