package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Locale;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2-build.270,2101.7.2-build.277]")
@Mixin(EnumComponent.class)
public class EnumComponentMixin {
    @ModifyExpressionValue(method = "lambda$new$4", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/type/EnumTypeInfo;getName(Ljava/lang/Object;)Ljava/lang/String;"))
    private static String toLowerCase(String original){
        return original.toLowerCase(Locale.ROOT);
    }
}
