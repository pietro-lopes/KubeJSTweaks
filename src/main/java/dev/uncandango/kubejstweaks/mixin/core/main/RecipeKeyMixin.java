package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RecipeKey.class)
public class RecipeKeyMixin {
    @Shadow
    public List<String> functionNames;

    @Shadow
    @Final
    public String name;

    @WrapMethod(method = "getPreferredBuilderKey")
    private String wrapGetPreferredBuilderKey(Operation<String> original) {
        return this.functionNames == null || functionNames.isEmpty() ? this.name : functionNames.getFirst();
    }
}
