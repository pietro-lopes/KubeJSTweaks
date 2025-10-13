package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.rhino.ContextFactory;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.invoke.MethodHandles;

@ConditionalMixin(modId = "rhino", versionRange = "[2101.2.7-build.74,2101.2.7-build.77]")
@Mixin(ContextFactory.class)
public abstract class ContextFactoryMixin {

    @WrapOperation(method = "getRecordConstructor", at= @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/ContextFactory;getMethodHandlesLookup()Ljava/lang/invoke/MethodHandles$Lookup;"))
    private MethodHandles.Lookup getPrivateIfNeeded(ContextFactory instance, Operation<MethodHandles.Lookup> original, @Local(argsOnly = true) Class<?> type){
        var originalLookup = original.call(instance);
        if (type.getConstructors().length == 0) {
            try {
                return MethodHandles.privateLookupIn(type, MethodHandles.lookup());
            } catch (IllegalAccessException e) {
                KubeJSTweaks.LOGGER.error("Failed to get private constructor.", e);
                return originalLookup;
            }
        }
        return originalLookup;
    }
}
