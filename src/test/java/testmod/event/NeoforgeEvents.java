package testmod.event;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.Cast;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import testmod.Utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

@EventBusSubscriber(modid = "testmod")
public class NeoforgeEvents {
    private static final Predicate<KubeRecipe> RECIPE_NOT_REMOVED = r -> r != null && !r.removed;

    @SubscribeEvent
    public static void onPackLoaded(AddReloadListenerEvent e){
        // NOT SAFE
        // THIS HAPPENS AT WORKER THREAD BUT WHATEVER PRAY FOR CME GODS
        ServerEvents.RECIPES.listenJava(ScriptType.SERVER, null, NeoforgeEvents::eventDispatcher);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarted(ServerStartedEvent e){
        testRecipes(e.getServer());
    }

    private static void testRecipes(MinecraftServer server){
        var event = KubeJSEvents.KUBEJS_EVENTS.get("recipes");
        var rm = server.getRecipeManager();
        KubeJS.LOGGER.info("onRecipeLoad started!");
        // THIS NEEDS TO BE CONDITIONALOPS TO FIX CRUSHER ORE RECIPE
        var registryOps = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        Stream.of(event.originalRecipes.values(),event.addedRecipes.stream().toList()).flatMap(Collection::stream).filter(RECIPE_NOT_REMOVED).forEach(kubeRecipe -> {
            if (kubeRecipe.type.schemaType.schema.keys.isEmpty()) return;
            if (kubeRecipe.id.getPath().contains("crusher/ore")){
                KubeJSTweaks.LOGGER.warn("Skipping crusher ore recipe {} due to weird codec behaviour", kubeRecipe.id + "[" + kubeRecipe.type + "]");
                return;
            }
            Recipe<?> originalRecipe;
            try {
                originalRecipe = rm.byKey(kubeRecipe.id).orElseThrow().value();
            } catch (NoSuchElementException e) {
                KubeJSTweaks.LOGGER.warn("Recipe {} not found", kubeRecipe.id + "[" + kubeRecipe.type + "]");
                return;
            }
            var codec = originalRecipe.getSerializer().codec();
            var originalJson = codec.encoder().encodeStart(registryOps, Cast.to(originalRecipe)).getOrThrow();
            for (var cv : kubeRecipe.getRecipeComponentValues()){
                if (cv.key.alwaysWrite) {
                    if (cv.value == null){
                        cv.value = Cast.to(cv.key.optional.getDefaultValue(kubeRecipe.type.schemaType));
                    }
                }
                if (cv.key.allowEmpty && cv.value == null) continue;
                if (!cv.key.optional() || cv.key.alwaysWrite) cv.write();
                if (cv.value != null && !cv.key.component.isEmpty(Cast.to(cv.value))) cv.write();
            }
            kubeRecipe.json = new JsonObject();
            try {
                kubeRecipe.serialize();
            } catch (Throwable e) {
                KubeJSTweaks.LOGGER.error("Failed to serialize recipe id {}", kubeRecipe.id + "[" + kubeRecipe.type + "]", e);
                return;
            }
            final var jsonKjs = kubeRecipe.json.deepCopy();

            Recipe<RecipeInput> tempKubeRecipe = null;
            try {
                Codec<Recipe<RecipeInput>> codec2 = (Codec<Recipe<RecipeInput>>) originalRecipe.getSerializer().codec().codec();
                tempKubeRecipe = codec2.decode(registryOps, jsonKjs).getOrThrow().getFirst();
            } catch (Exception e) {
                KubeJSTweaks.LOGGER.error("Error parsing recipe {}", kubeRecipe.id + "[" + kubeRecipe.type + "]", e);
                return;
            }

            var newJsonKjs = (JsonObject) codec.encoder().encodeStart(registryOps, Cast.to(tempKubeRecipe)).getOrThrow();

            LinkedHashMap<String, Object> originalMap = Cast.to(Utils.unwrap(originalJson));
            LinkedHashMap<String, Object> kubejsMap = Cast.to(Utils.unwrap(newJsonKjs));
            var diff = Maps.difference(originalMap, kubejsMap);
            if (!diff.areEqual()){
                KubeJSTweaks.LOGGER.warn("Recipe {} does not encode the same.", kubeRecipe.id + "[" + kubeRecipe.type + "]");
                var differing = diff.entriesDiffering();
                if (!differing.isEmpty()) {
                    KubeJSTweaks.LOGGER.warn("Entries differing\n--------------------------");
                    differing.forEach((key, value) -> KubeJSTweaks.LOGGER.warn("{}: {}", key, value));
                }
                var onlyOnOriginal = diff.entriesOnlyOnLeft();
                if (!onlyOnOriginal.isEmpty()) {
                    KubeJSTweaks.LOGGER.warn("Entries only on original\n--------------------------");
                    onlyOnOriginal.forEach((key, value) -> KubeJSTweaks.LOGGER.warn("{}: {}", key, value));
                }
                var onlyOnKjs = diff.entriesOnlyOnRight();
                if (!onlyOnKjs.isEmpty()) {
                    KubeJSTweaks.LOGGER.warn("Entries only on kjs\n--------------------------");
                    onlyOnKjs.forEach((key, value) -> KubeJSTweaks.LOGGER.warn("{}: {}", key, value));
                }
            }
        });
    }

    public static Object eventDispatcher(KubeEvent kubeEvent) {
        switch (kubeEvent){
            case RecipesKubeEvent recipes -> KubeJSEvents.onRecipes(recipes);
            case null, default -> {}
        }
        return EventResult.PASS;
    }
}

