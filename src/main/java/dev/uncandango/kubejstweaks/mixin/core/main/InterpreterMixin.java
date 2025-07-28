package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Interpreter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Interpreter.class)
public class InterpreterMixin {
    @WrapOperation(method = "getSourcePositionFromStack", at = @At(value = "FIELD", target = "Ldev/latvian/mods/rhino/Context;lastInterpreterFrame:Ljava/lang/Object;"))
    private Object checkLastFrameIsNull(Context instance, Operation<Object> original, @Cancellable CallbackInfoReturnable<String> cir){
        var result = original.call(instance);
        if (result == null) {
            cir.setReturnValue(null);
        }
        return result;
    }
}
