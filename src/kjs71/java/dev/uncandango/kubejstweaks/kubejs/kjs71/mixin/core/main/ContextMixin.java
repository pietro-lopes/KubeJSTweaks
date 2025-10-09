package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Function;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Fixed by ZZZank Generics PR
@Mixin(Context.class)
public class ContextMixin {
    @ModifyExpressionValue(method = "getConversionWeight", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/type/TypeInfo;is(Ldev/latvian/mods/rhino/type/TypeInfo;)Z", ordinal = 0))
    private boolean moreRestrictCheck(boolean original, Object from){
        if (from instanceof Function) return false;
        return original;
    }

    @Definition(id = "OBJECT", field = "Ldev/latvian/mods/rhino/type/TypeInfo;OBJECT:Ldev/latvian/mods/rhino/type/TypeInfo;")
    @Definition(id = "target", local = @Local(type = TypeInfo.class, argsOnly = true))
    @Expression("target == OBJECT")
    @ModifyExpressionValue(method = "getConversionWeight", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean checkUnboundedGeneric(boolean original, @Local(argsOnly = true) TypeInfo target){
        if (!original && target instanceof VariableTypeInfo vti) {
            return !vti.shouldConvert();
        } else {
            return original;
        }
    }
}
