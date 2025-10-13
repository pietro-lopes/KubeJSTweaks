package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedKubeRecipe;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(ShapedKubeRecipe.class)
public class ShapedKubeRecipeMixin {

//    @Inject(method = "afterLoaded", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(CC)Ljava/lang/String;"))
//    private void throwWhenEmptyIngredient(CallbackInfo ci, @Local Character a){
//        throw new KubeRuntimeException("Recipe " + this + " contains an empty ingredient at key: " + a);
//    }
}
