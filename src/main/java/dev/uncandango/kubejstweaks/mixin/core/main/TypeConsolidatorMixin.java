package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.rhino.type.TypeConsolidator;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@ConditionalMixin(modId = "rhino", versionRange = "[2101.2.7-build.77]")
@Mixin(TypeConsolidator.class)
public class TypeConsolidatorMixin {
    @WrapOperation(method = "consolidateAll(Ljava/util/List;Ljava/util/Map;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Ljava/util/List;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private static Object populateIfOOB(List<Object> instance, int i, Object e, Operation<Object> original){
        while (instance.size() <= i) {
            instance.add(null);
        }
        return original.call(instance, i, e);
    }
}
