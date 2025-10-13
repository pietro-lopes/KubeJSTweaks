package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.error.InvalidRecipeComponentException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.schema.UnknownRecipeSchema;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ConsoleLine;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.uncandango.kubejstweaks.kubejs.debug.DumpErroringRecipes;
import dev.uncandango.kubejstweaks.kubejs.event.PreRecipeEventJS;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.regex.Pattern;

// https://github.com/KubeJS-Mods/KubeJS/issues/1031
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RecipesKubeEvent.class)
public abstract class RecipesKubeEventMixin {
    @Shadow
    @Final
    public Map<ResourceLocation, KubeRecipe> originalRecipes;

    @Shadow
    protected abstract void infoSkip(String s);

    @Inject(method = "parseOriginalRecipe", at = @At(value = "JUMP", opcode = Opcodes.GOTO, ordinal = 2))
    private void logError(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local InvalidRecipeComponentException error, @Local(ordinal = 1) String recipeIdAndType, @Local RecipeTypeFunction type){
        if (DevProperties.get().logErroringRecipes) {
            if (PreRecipeEventJS.shouldIgnoreWarning(recipeId)) {
                ConsoleJS.SERVER.info("Recipe " + recipeIdAndType + " failed to parse due to an error on component: " + error);
            } else {
                ConsoleJS.SERVER.warn("Recipe " + recipeIdAndType + " failed to parse due to an error on component: " + error);
            }
        }
        if (DumpErroringRecipes.isEnabled() && !PreRecipeEventJS.shouldIgnoreWarning(recipeId)) {
            DumpErroringRecipes.add(error, recipeId, json);
        }
        try {
            originalRecipes.put(recipeId, UnknownRecipeSchema.SCHEMA.deserialize(SourceLine.UNKNOWN, type, recipeId, json));
        } catch (NullPointerException | IllegalArgumentException | JsonParseException ex2) {
            if (DevProperties.get().logErroringRecipes) {
                ConsoleJS.SERVER.error("Failed to parse recipe " + recipeIdAndType, ex2, RecipesKubeEvent.POST_SKIP_ERROR);
            }
            DumpErroringRecipes.add(ex2, recipeId, json);
        } catch (Exception ex3) {
            ConsoleJS.SERVER.error("Failed to parse recipe " + recipeIdAndType, ex3, RecipesKubeEvent.POST_SKIP_ERROR);
            DumpErroringRecipes.add(ex3, recipeId, json);
        }
    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/DevProperties;get()Ldev/latvian/mods/kubejs/DevProperties;", shift = At.Shift.BEFORE, ordinal = 0))
    private void dumpErroringRecipes(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local Throwable error){
        DumpErroringRecipes.add(error, recipeId, json);
    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/DevProperties;get()Ldev/latvian/mods/kubejs/DevProperties;", shift = At.Shift.BEFORE, ordinal = 1))
    private void dumpErroringRecipes5(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local RuntimeException error){
        DumpErroringRecipes.add(error, recipeId, json);
    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;", ordinal = 2))
    private void dumpErroringRecipes2(JsonObject json, ResourceLocation recipeId, CallbackInfo ci, @Local Exception error){
        DumpErroringRecipes.add(error, recipeId, json);
    }

    @Inject(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/RecipesKubeEvent;warnSkip(Ljava/lang/String;)V"))
    private void dumpErroringRecipes4(JsonObject json, ResourceLocation recipeId, CallbackInfo ci){
        DumpErroringRecipes.add(new IllegalArgumentException("Unknown recipe type"), recipeId, json);
    }

    @Redirect(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;", ordinal = 1))
    private ConsoleLine warnToError1(ConsoleJS instance, String message, Throwable error, Pattern exitPattern){
        return instance.error(message, error, exitPattern);
    }

    @Redirect(method = "parseOriginalRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;", ordinal = 2))
    private ConsoleLine warnToError2(ConsoleJS instance, String message, Throwable error, Pattern exitPattern){
        return instance.error(message, error, exitPattern);
    }

    @Redirect(method = "discoverRecipes", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/RecipesKubeEvent;warnSkip(Ljava/lang/String;)V", ordinal = 1))
    private void reduceToInfo(RecipesKubeEvent instance, String s){
        infoSkip(s.replace("not a json object", "no type defined"));
    }

    @Redirect(method = "handleFailedRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ljava/lang/Throwable;)Ldev/latvian/mods/kubejs/script/ConsoleLine;"))
    private ConsoleLine warnToError(ConsoleJS instance, String message, Throwable error){
        return instance.error(message, error);
    }

    @Inject(method = "discoverRecipes", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/RecipesKubeEvent;errorSkip(Ljava/lang/String;)V"))
    private void dumpErroringRecipes3(RecipeManagerKJS recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci, @Local JsonElement originalJson, @Local ResourceLocation recipeId, @Local DataResult.Error<Object> error){
        DumpErroringRecipes.add(new IllegalArgumentException(error.message()), recipeId, originalJson);
    }
}
