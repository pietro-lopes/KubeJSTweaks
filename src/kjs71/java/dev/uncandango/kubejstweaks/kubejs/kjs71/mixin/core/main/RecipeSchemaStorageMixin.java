package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaStorage;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RecipeSchemaStorage.class)
public class RecipeSchemaStorageMixin {
    @ModifyExpressionValue(method = "fireEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;", ordinal = 0))
    private static Map<ResourceLocation, Resource> loadSpecificDatapack(Map<ResourceLocation, Resource> original, @Local(argsOnly = true) ResourceManager manager){
        if (ModList.get().getModFileById("kubejs").versionString().startsWith("2101.7.1-")) {
            var kjs71 = manager.listResources("kubejs/kjs71", path -> path.getPath().endsWith("/recipe_mappings.json"));
            kjs71.forEach((rl, rs) -> {
                original.put(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(),rl.getPath().replace("/kjs71","")), rs);
            });
        }
        return original;
    }

    @ModifyExpressionValue(method = "fireEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;", ordinal = 1))
    private static Map<ResourceLocation, Resource> loadSpecificDatapack2(Map<ResourceLocation, Resource> original, @Local(argsOnly = true) ResourceManager manager){
        if (ModList.get().getModFileById("kubejs").versionString().startsWith("2101.7.1-")) {
            var kjs71 = manager.listResources("kubejs/kjs71", path -> path.getPath().endsWith("/recipe_components.json"));
            kjs71.forEach((rl, rs) -> {
                original.put(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(),rl.getPath().replace("/kjs71","")), rs);
            });
        }
        return original;
    }
}
