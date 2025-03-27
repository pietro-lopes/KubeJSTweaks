package dev.uncandango.kubejstweaks.mixin;

import com.google.common.collect.Maps;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import dev.uncandango.kubejstweaks.mixin.annotation.Helper;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;

public class ConditionalMixinManager {
    private Map<String, Boolean> conditionalMixins = Maps.newHashMap();

    public ConditionalMixinManager() {
        @SuppressWarnings("UnstableApiUsage")
        var scanData = LoadingModList.get().getModFileById("kubejstweaks").getFile().getScanResult();
        scanData.getAnnotatedBy(ConditionalMixin.class, ElementType.TYPE).forEach(annotation -> {
            String currentMixin = annotation.memberName();
            Boolean shouldLoad = null;

            String modId = Helper.getValue(annotation, "modId");
            String versionRange = Helper.getValue(annotation, "versionRange");
            List<String> extraModDep = Helper.getValue(annotation, "extraModDep");
            List<String> extraModDepVersions = Helper.getValue(annotation, "extraModDepVersions");
            String config = Helper.getValue(annotation, "config");
            Boolean devOnly = Helper.getValue(annotation, "devOnly");

            ModFileInfo mod = LoadingModList.get().getModFileById(modId);

            if (Boolean.TRUE.equals(devOnly)) {
                if (FMLEnvironment.production) {
                    KubeJSTweaks.LOGGER.info("Conditional Mixin {} will not be loaded in production", currentMixin);
                    shouldLoad = false;
                }
            }

            // TODO: Add config support
            // config

            if (mod != null && shouldLoad == null) {
                try {
                    VersionRange range = VersionRange.createFromVersionSpec(versionRange);
                    var modVerString = mod.versionString();
                    var modVer = new DefaultArtifactVersion(modVerString);
                    if (range != null && range.containsVersion(modVer) || modVerString.equals("0.0NONE")) {
                        // TODO: Add Extra mods support
                        // extraModDep
                        // extraModDepVersions

                        KubeJSTweaks.LOGGER.info("Conditional Mixin {} will be loaded as it matches versions: {} in {}", currentMixin, modVer, range);
                        shouldLoad = true;
                    } else {
                        shouldLoad = false;
                    }
                } catch (InvalidVersionSpecificationException e) {
                    KubeJSTweaks.LOGGER.error("Conditional Mixin {} with Invalid Version Spec {}", currentMixin, versionRange);
                    shouldLoad = false;
                }
            } else {
                KubeJSTweaks.LOGGER.debug("Conditional Mixin {} with Mod {} not found", currentMixin, modId);
            }
            conditionalMixins.put(currentMixin, shouldLoad != null && shouldLoad);
        });
    }

    public boolean shouldLoad(String mixinClassName) {
        return conditionalMixins.getOrDefault(mixinClassName, true);
    }
}
