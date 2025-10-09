package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// https://github.com/KubeJS-Mods/KubeJS/commit/b8d43110053a20395cfcaa2b53286a0dbdf9a69d
// https://github.com/KubeJS-Mods/KubeJS/issues/878
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(targets = "dev.latvian.mods.kubejs.recipe.CachedTagLookup$1")
public class CachedTagLookupMixin<T> {
    @Redirect(method = "element", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;"))
    private Object redirectElement(Registry<T> instance, ResourceLocation resourceLocation) {
        return instance.getOptional(resourceLocation).orElse(null);
    }
}

