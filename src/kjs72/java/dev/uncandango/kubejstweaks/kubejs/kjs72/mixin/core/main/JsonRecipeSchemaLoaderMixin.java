package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaLoader;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2,2101.7.3)")
@Mixin(JsonRecipeSchemaLoader.class)
public class JsonRecipeSchemaLoaderMixin {

    @ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"), index = 1)
    private static Object checkVersion(Object par2, @Local ResourceLocation id){
        if (par2 instanceof JsonObject json) {
            if (!ModList.get().isLoaded(id.getNamespace())) {
                json.asMap().clear();
                return json;
            }
            if (json.has("keys")) {
                var it = json.getAsJsonArray("keys").iterator();
                while (it.hasNext()) {
                    var key = it.next();
                    if (key instanceof JsonObject keyJson) {
                        if (keyJson.has("kubejstweaks:version_range")) {
                            var range = keyJson.getAsJsonPrimitive("kubejstweaks:version_range").getAsString();
                            var modId = id.getNamespace();
                            KubeJSTweaks.LOGGER.debug("Mod id {} with conditional version {}", modId, range);
                            try {
                                var rangeSpec = VersionRange.createFromVersionSpec(range);
                                var modFile = ModList.get().getModFileById(modId);
                                if (modFile != null) {
                                    var modVersion = new DefaultArtifactVersion(modFile.versionString());
                                    if (rangeSpec.containsVersion(modVersion)){
                                        KubeJSTweaks.LOGGER.debug("Key component for type {} with version range {} matches with mod version {}.", id, range, modVersion);
                                    } else {
                                        KubeJSTweaks.LOGGER.debug("Key component for type {} with version range {} DOES NOT match with mod version {}.", id, range, modVersion);
                                        it.remove();
                                    }
                                } else {
                                    KubeJSTweaks.LOGGER.warn("Mod {} was not found for key component type {}.", modId, id);
                                }
                            } catch (InvalidVersionSpecificationException ex) {
                                KubeJSTweaks.LOGGER.error("Error evaluating version for a key component for mod: " + modId, ex);
                            }
                        }
                    }
                }
            }
        }
        return par2;
    }
}
