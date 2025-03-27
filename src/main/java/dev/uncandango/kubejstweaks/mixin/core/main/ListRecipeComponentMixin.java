package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;

// https://github.com/KubeJS-Mods/KubeJS/commit/7be633bf94ac17905cc96d915d773c03b3af05d5
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(ListRecipeComponent.class)
public class ListRecipeComponentMixin {

}
