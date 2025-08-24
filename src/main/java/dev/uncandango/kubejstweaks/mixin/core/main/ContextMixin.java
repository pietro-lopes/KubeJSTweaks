package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Context.class)
public class ContextMixin {
    @ModifyExpressionValue(method = "getConversionWeight", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/type/TypeInfo;is(Ldev/latvian/mods/rhino/type/TypeInfo;)Z", ordinal = 0))
    private boolean moreRestrictCheck(boolean original, Object from){
        if (from instanceof Function) return false;
        return original;
    }
}
