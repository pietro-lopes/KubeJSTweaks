package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RegistryComponent;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

// https://github.com/KubeJS-Mods/KubeJS/commit/4a5391bf2257f6b97d02ac2227089bd8b695775c
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RegistryComponent.class)
public abstract class RegistryComponentMixin<T> implements RecipeComponent<T> {
    @Shadow
    @Final
    private Registry<T> registry;

    @ModifyArg(method = "lambda$static$0", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/component/RegistryComponent;<init>(Lnet/minecraft/core/Registry;Ldev/latvian/mods/kubejs/registry/RegistryType;Lcom/mojang/serialization/Codec;)V"), index = 2)
    private static <T> Codec<T> modifyArg(Codec<T> codec, @Local ResourceKey<Registry<T>> key, @Local(argsOnly = true) RegistryAccessContainer access) {
        return access.access().registry(key).orElseThrow().byNameCodec();
    }

    @Override
    public boolean isEmpty(T value) {
        if (value instanceof Optional<?> op){
            if (op.isEmpty()) {
                return true;
            } else {
                this.isEmpty((T)op.get());
            }
        }
        if (this.registry instanceof DefaultedRegistry<T> dr){
            return dr.get(dr.getDefaultKey()).equals(value);
        }
        return value == null;
    }
}
