//package dev.uncandango.kubejstweaks.mixin.core.main;
//
//import com.google.common.base.Stopwatch;
//import com.google.gson.JsonElement;
//import com.mojang.serialization.JsonOps;
//import com.simibubi.create.AllTags;
//import dev.uncandango.kubejstweaks.KubeJSTweaks;
//import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.ResourceManager;
//import net.minecraft.util.profiling.ProfilerFiller;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeManager;
//import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.HashMap;
//import java.util.Map;
//
//// To be removed, Create already came up with a solution
//@ConditionalMixin(modId = "create", versionRange = "6.0.0")
//@Mixin(RecipeManager.class)
//public class RecipeManagerMixin {
//    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
//    private void injectSmithingTrims(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
//        var stopwatch = Stopwatch.createStarted();
//        Map<ResourceLocation, JsonElement> replaced = new HashMap<>();
//        object.forEach((recipeId, json) -> {
//            var type = json.getAsJsonObject().get("type");
//            if (type != null && type.getAsString().equals("minecraft:smithing_trim")){
//                KubeJSTweaks.LOGGER.info("Adding Smithing Trim to replace: {}", recipeId);
//                replaced.put(recipeId, json);
//            }
//        });
//        replaced.forEach((recipeId, json) -> {
//            JsonElement ingJson = json.getAsJsonObject().get("base");
//            Ingredient base = Ingredient.CODEC.parse(JsonOps.INSTANCE, ingJson).getOrThrow();
//            Ingredient merged = new DifferenceIngredient(base, Ingredient.of(AllTags.AllItemTags.DIVING_ARMOR.tag)).toVanilla();
//            JsonElement newBaseJson = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, merged).getOrThrow();
//            json.getAsJsonObject().add("base", newBaseJson);
//        });
//        KubeJSTweaks.LOGGER.info("Replaced {} Smithing Trims in {} ms", replaced.size(), stopwatch.elapsed().toMillis());
//    }
//}
