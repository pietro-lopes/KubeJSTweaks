package dev.uncandango.kubejstweaks.mixin.core.main;

import com.google.gson.JsonElement;
import dev.uncandango.kubejstweaks.kubejs.event.KJSTEvents;
import dev.uncandango.kubejstweaks.kubejs.event.PreRecipeEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 1098)
public class RecipeManagerMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void kjstweaks$triggerBeforeRecipeEvent(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        var preRecipeEvent = new PreRecipeEventJS(object);
        KJSTEvents.preRecipes.post(preRecipeEvent);
    }
}
