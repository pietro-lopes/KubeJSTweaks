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
                        if (!extraModDep.isEmpty()) {
                            for (int i = 0; i < extraModDep.size(); i++) {
                                var dep = LoadingModList.get().getModFileById(extraModDep.get(i));
                                if (dep == null) {
                                    KubeJSTweaks.LOGGER.debug("Extra dependency Mod {} is not present in the mod list", extraModDep.get(i));
                                    KubeJSTweaks.LOGGER.debug("Class {} will NOT be loaded as extra mod dependecy is not present.", annotation.memberName());
                                    shouldLoad = false;
                                } else {
                                    var rangeDepVer = VersionRange.createFromVersionSpec(extraModDepVersions.get(i));
                                    var depVerString = dep.versionString();
                                    var depVer = new DefaultArtifactVersion(depVerString);
                                    if (rangeDepVer.containsVersion(depVer) || depVerString.equals("0.0NONE")) {
                                        KubeJSTweaks.LOGGER.info("Extra dependecy Mod {} matches versions: {} in {}", extraModDep.get(i),dep.versionString(), extraModDepVersions.get(i));
                                    } else {
                                        KubeJSTweaks.LOGGER.debug("Extra dependecy Mod {} does NOT matches versions: {} in {}", extraModDep.get(i),dep.versionString(), extraModDepVersions.get(i));
                                        KubeJSTweaks.LOGGER.debug("Class {} will NOT be loaded as extra mod dependecy does not match.", annotation.memberName());
                                        shouldLoad = false;
                                    }
                                }
                            }
                            if (shouldLoad == null) {
                                KubeJSTweaks.LOGGER.info("Conditional Mixin {} will be loaded as it matches versions: {} in {}", currentMixin, modVer, range);
                                shouldLoad = true;
                            }
                        } else {
                            KubeJSTweaks.LOGGER.info("Conditional Mixin {} will be loaded as it matches versions: {} in {}", currentMixin, modVer, range);
                            shouldLoad = true;
                        }
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
