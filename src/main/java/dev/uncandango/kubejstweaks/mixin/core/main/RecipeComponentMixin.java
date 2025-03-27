package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.uncandango.kubejstweaks.mixin.Utils;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// https://github.com/KubeJS-Mods/KubeJS/commit/7be633bf94ac17905cc96d915d773c03b3af05d5
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RecipeComponent.class)
public interface RecipeComponentMixin {
    @WrapOperation(method = {"asList","asListOrSelf","asConditionalList","asConditionalListOrSelf"}, at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;create(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZZ)Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;"))
    private ListRecipeComponent redirectToNewRecord(RecipeComponent component, boolean canWriteSelf, boolean conditional, Operation<ListRecipeComponent> original){
        return Utils.getRecipeListComponent(new Object[]{component, canWriteSelf, conditional, false});
    }
}
