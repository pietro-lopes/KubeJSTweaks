package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.rhino.CachedClassInfo;
import dev.latvian.mods.rhino.NativeJavaMethod;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@ConditionalMixin(modId = "rhino", versionRange = "[2101.2.7-build.74,2101.2.7-build.77]")
@Mixin(NativeJavaMethod.class)
public class NativeJavaMethodMixin {
    @Redirect(method = "call", at = @At(value = "INVOKE", target = "Ljava/lang/Object;getClass()Ljava/lang/Class;"))
    private Class<?> getRealClass(Object instance){
        return ((CachedClassInfo)instance).type;
    }
}
