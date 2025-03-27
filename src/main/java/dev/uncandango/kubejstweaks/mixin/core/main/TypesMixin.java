package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@ConditionalMixin(modId = "probejs", versionRange = "7.5.1")
@Mixin(Types.class)
public interface TypesMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lmoe/wolfgirl/probejs/lang/typescript/code/type/js/JSPrimitiveType;<init>(Ljava/lang/String;)V", ordinal = 3))
    private static String modifyString(String str){
        return "StringJS";
    }
}
