package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thedarkcolour.exdeorum.compat.kubejs.ExDeorumKubeJsPlugin;

// We cancel this because ProbeJS does not generate schemas from json files
@ConditionalMixin(modId = "exdeorum", versionRange = "3.6")
@Mixin(ExDeorumKubeJsPlugin.class)
public class ExDeorumKubeJsPluginMixin {
    @Inject(method = "registerRecipeSchemas", at = @At("HEAD"), cancellable = true)
    private void cancelBuiltinSchemas(RecipeSchemaRegistry registry, CallbackInfo ci){
        ci.cancel();
    }
}
