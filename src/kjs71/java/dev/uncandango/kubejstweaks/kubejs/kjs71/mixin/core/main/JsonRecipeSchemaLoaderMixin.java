package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaLoader;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.List;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(JsonRecipeSchemaLoader.class)
public class JsonRecipeSchemaLoaderMixin {
    @ModifyReceiver(method = "load", at= @At(value = "FIELD", target = "Ldev/latvian/mods/kubejs/recipe/schema/JsonRecipeSchemaLoader$RecipeSchemaBuilder;keys:Ljava/util/List;", ordinal = 1))
    private static @Coerce Object grabHolder(@Coerce Object instance, @Share("holder") LocalRef<RecipeSchemaBuilderAccessor> holderRef){
        holderRef.set((RecipeSchemaBuilderAccessor)instance);
        return instance;
    }

    @WrapWithCondition(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private static boolean checkVersion(List<Object> instance, Object e, @Local JsonObject keyJson, @Share("holder") LocalRef<RecipeSchemaBuilderAccessor> holderRef){
        var type = holderRef.get().kjstweaks$getId();
        var modId = type.getNamespace();
        if (!ModList.get().isLoaded(modId)) {
            keyJson.asMap().clear();
            KubeJSTweaks.LOGGER.info("Skipping schema {} for mod NOT loaded: {}", type, type.getNamespace());
            return false;
        }
        if (keyJson.has("kubejstweaks:version_range")) {
            var range = keyJson.getAsJsonPrimitive("kubejstweaks:version_range").getAsString();
            KubeJSTweaks.LOGGER.debug("Mod id {} with conditional version {}", modId, range);
            try {
                var rangeSpec = VersionRange.createFromVersionSpec(range);
                var modFile = ModList.get().getModFileById(modId);
                if (modFile != null) {
                    var modVersion = new DefaultArtifactVersion(modFile.versionString());
                    if (rangeSpec.containsVersion(modVersion)){
                        KubeJSTweaks.LOGGER.debug("Key component for type {} with version range {} matches with mod version {}.", type, range, modVersion);
                        return true;
                    } else {
                        KubeJSTweaks.LOGGER.debug("Key component for type {} with version range {} DOES NOT match with mod version {}.", type, range, modVersion);
                    }
                } else {
                    KubeJSTweaks.LOGGER.warn("Mod {} was not found for key component type {}.", modId, type);
                }
            } catch (InvalidVersionSpecificationException ex) {
                KubeJSTweaks.LOGGER.error("Error evaluating version for a key component for mod: " + modId, ex);
            }
            return false;
        } else {
            return true;
        }
    }

    @Mixin(targets = "dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaLoader$RecipeSchemaBuilder")
    public interface RecipeSchemaBuilderAccessor {
        @Accessor("id")
        ResourceLocation kjstweaks$getId();
    }
}
