package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistryAccessContainer.class)
public class RegistryAccessContainerMixin {
    @WrapOperation(method = "decodeJson(Lcom/mojang/serialization/Codec;Lcom/google/gson/JsonElement;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;decode(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private DataResult kjstweaks$throwProperly(Codec instance, DynamicOps dynamicOps, Object o, Operation<DataResult> original){
        var result = original.call(instance, dynamicOps, o);
        result.getOrThrow();
        return result;
    }
}
