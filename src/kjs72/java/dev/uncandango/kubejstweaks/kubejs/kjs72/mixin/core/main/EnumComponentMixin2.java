package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.rhino.type.EnumTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2-build.279]")
@Mixin(EnumComponent.class)
public class EnumComponentMixin2 {
    @ModifyExpressionValue(method = "lambda$static$1", at = @At(value = "INVOKE", target = "Ljava/lang/Class;isAssignableFrom(Ljava/lang/Class;)Z"))
    private static boolean fixClassCheck(boolean original, @Local(argsOnly = true) EnumTypeInfo typeInfo){
        return StringRepresentable.class.isAssignableFrom(typeInfo.asClass());
    }
}
