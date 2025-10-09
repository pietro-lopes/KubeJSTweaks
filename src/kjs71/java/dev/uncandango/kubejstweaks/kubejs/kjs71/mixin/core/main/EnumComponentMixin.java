package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.mojang.brigadier.StringReader;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.rhino.type.EnumTypeInfo;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;
import java.util.function.Function;

// https://github.com/KubeJS-Mods/KubeJS/commit/fabaff1f05068beaf3923a53251289ace4a8a1a4
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(EnumComponent.class)
public class EnumComponentMixin {
    @Redirect(method = "lambda$static$1", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;expect(C)V", ordinal = 1))
    private static void cancelFunctionCall(StringReader instance, char c){
        // NO-OP
    }

    @ModifyArg(method = "lambda$static$1", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/PrimitiveCodec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"), index = 1)
    private static Function<Enum<?>,String> toLowerCase(Function<Enum<?>,String> par1){
        return (e) -> EnumTypeInfo.getName(e).toLowerCase(Locale.ROOT);
    }
}
