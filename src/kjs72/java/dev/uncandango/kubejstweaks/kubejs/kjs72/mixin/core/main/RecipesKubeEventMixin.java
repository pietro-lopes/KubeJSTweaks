package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ConsoleLine;
import dev.uncandango.kubejstweaks.kubejs.debug.DumpErroringRecipes;
import dev.uncandango.kubejstweaks.kubejs.event.PreRecipeEventJS;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.regex.Pattern;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2,2101.7.3)")
@Mixin(RecipesKubeEvent.class)
public abstract class RecipesKubeEventMixin {
    @Shadow
    @Final
    public Map<ResourceLocation, KubeRecipe> originalRecipes;

    @Shadow
    protected abstract void infoSkip(String s);

    @Redirect(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;"))
    private ConsoleLine skipWarningIfIgnore(ConsoleJS instance, String message, Throwable errorTh, Pattern exitPattern, @Local(argsOnly = true) ResourceLocation recipeId, @Local(argsOnly = true) JsonObject json,  @Local(ordinal = 1) String recipeIdAndType, @Local RecipeTypeFunction type){
        if (PreRecipeEventJS.shouldIgnoreWarning(recipeId)) {
            return instance.info("Recipe " + recipeIdAndType + " failed to parse due to an error on component: " + errorTh);
        } else {
            DumpErroringRecipes.add(errorTh, recipeId, json);
            return instance.warn("Recipe " + recipeIdAndType + " failed to parse due to an error on component: " + errorTh);
        }
    }

//    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/DevProperties;get()Ldev/latvian/mods/kubejs/DevProperties;", shift = At.Shift.BEFORE, ordinal = 0))
//    private void dumpErroringRecipes(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local Throwable error){
//        DumpErroringRecipes.add(error, recipeId, json);
//    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/DevProperties;get()Ldev/latvian/mods/kubejs/DevProperties;", shift = At.Shift.BEFORE, ordinal = 1))
    private void dumpErroringRecipes5(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local RuntimeException error){
        DumpErroringRecipes.add(error, recipeId, json);
    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;error(Ljava/lang/String;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;", ordinal = 1))
    private void dumpErroringRecipes2(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local Exception error){
        DumpErroringRecipes.add(error, recipeId, json);
    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/RecipesKubeEvent;warnSkip(Ljava/lang/String;)V"))
    private void dumpErroringRecipes4(JsonObject json, ResourceLocation recipeId, CallbackInfo ci){
        DumpErroringRecipes.add(new IllegalArgumentException("Unknown recipe type"), recipeId, json);
    }

    @Redirect(method = "discoverRecipes", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/RecipesKubeEvent;warnSkip(Ljava/lang/String;)V", ordinal = 1))
    private void reduceToInfo(RecipesKubeEvent instance, String s){
        infoSkip(s.replace("not a json object", "no type defined"));
    }

    @Inject(method = "discoverRecipes", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/RecipesKubeEvent;errorSkip(Ljava/lang/String;)V"))
    private void dumpErroringRecipes3(RecipeManagerKJS recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci, @Local JsonElement originalJson, @Local ResourceLocation recipeId, @Local DataResult.Error<Object> error){
        DumpErroringRecipes.add(new IllegalArgumentException(error.message()), recipeId, originalJson);
    }
}
