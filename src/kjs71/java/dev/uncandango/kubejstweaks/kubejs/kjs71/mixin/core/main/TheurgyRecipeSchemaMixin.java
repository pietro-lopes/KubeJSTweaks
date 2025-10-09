package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.klikli_dev.theurgykubejs.TheurgyRecipeSchema;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@ConditionalMixin(modId = "theurgy_kubejs", versionRange = "*")
@Mixin(TheurgyRecipeSchema.class)
public interface TheurgyRecipeSchemaMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/component/SizedFluidIngredientComponent;inputKey(Ljava/lang/String;)Ldev/latvian/mods/kubejs/recipe/RecipeKey;", ordinal = 0), require = 0)
    private static RecipeKey<?> kjstweaks$makeItOptional(RecipeKey<?> original){
        return original.defaultOptional();
    }
}
