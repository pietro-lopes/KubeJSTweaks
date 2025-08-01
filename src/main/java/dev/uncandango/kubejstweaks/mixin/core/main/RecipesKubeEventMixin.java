package dev.uncandango.kubejstweaks.mixin.core.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.error.InvalidRecipeComponentException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.schema.UnknownRecipeSchema;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RecipesKubeEvent.class)
public class RecipesKubeEventMixin {
    @Shadow
    @Final
    public Map<ResourceLocation, KubeRecipe> originalRecipes;

    @Inject(method = "parseOriginalRecipe", at = @At(value = "JUMP", opcode = Opcodes.GOTO, ordinal = 2))
    private void logError(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local InvalidRecipeComponentException error, @Local(ordinal = 1) String recipeIdAndType, @Local RecipeTypeFunction type){
        if (DevProperties.get().logErroringRecipes) {
            ConsoleJS.SERVER.warn("Recipe " + recipeIdAndType + " failed to parse due to an error on component: " + error);
        }
        try {
            originalRecipes.put(recipeId, UnknownRecipeSchema.SCHEMA.deserialize(SourceLine.UNKNOWN, type, recipeId, json));
        } catch (NullPointerException | IllegalArgumentException | JsonParseException ex2) {
            if (DevProperties.get().logErroringRecipes) {
                ConsoleJS.SERVER.warn("Failed to parse recipe " + recipeIdAndType, ex2, RecipesKubeEvent.POST_SKIP_ERROR);
            }
        } catch (Exception ex3) {
            ConsoleJS.SERVER.warn("Failed to parse recipe " + recipeIdAndType, ex3, RecipesKubeEvent.POST_SKIP_ERROR);
        }
    }
}
