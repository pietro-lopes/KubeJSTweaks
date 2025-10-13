package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaLoader;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2,2101.7.3)")
@Mixin(JsonRecipeSchemaLoader.class)
public class JsonRecipeSchemaLoaderMixin2 {
    @ModifyExpressionValue(method = "load", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"))
    private static Map<ResourceLocation, Resource> loadSpecificDatapack(Map<ResourceLocation, Resource> original, @Local(argsOnly = true) ResourceManager manager){
        if (ModList.get().getModFileById("kubejs").versionString().startsWith("2101.7.2-")) {
            var kjs72 = manager.listResources("kubejs/kjs72/recipe_schema", path -> path.getPath().endsWith(".json"));
            kjs72.forEach((rl, rs) -> {
                original.put(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(),rl.getPath().replace("/kjs72","")), rs);
            });
        }
        return original;
    }
}
