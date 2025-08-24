package dev.uncandango.kubejstweaks.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.ArrayList;
import java.util.List;

public class CompatibilityEventJS implements KubeEvent {
    private final List<String> messages = new ArrayList<>();

    public void checkModLoaded(String mod, String reason) {
        if (ModList.get().isLoaded(mod)) {
            addIncompatibility("Incompatible mod loaded: " + mod, reason);
        } else {
            KubeJSTweaks.LOGGER.debug("Incompatible mod {} was not loaded.", mod);
        }
    }

    public void checkModVersion(String mod, String version, String reason) {
        var modFile = ModList.get().getModFileById(mod);
        if (modFile == null) {
            KubeJSTweaks.LOGGER.debug("Mod with id {} was not found!", mod);
        } else {
            try {
                if (!version.contains("[") && !version.contains("]")) {
                    version = "[" + version + "]";
                }
                var versionRange = VersionRange.createFromVersionSpec(version);
                var modVer = new DefaultArtifactVersion(modFile.versionString());
                if (versionRange.containsVersion(modVer)) {
                    KubeJSTweaks.LOGGER.debug("Incompatible mod {} version matches {} with {}", mod, versionRange, modVer);
                    addIncompatibility("Incompatible version " + modFile.versionString() + " for mod " + mod, reason);
                }
            } catch (InvalidVersionSpecificationException e) {
                KubeJSTweaks.LOGGER.error("Error while checking mod " + mod + " version", e);
            }
        }
    }

    public void addIncompatibility(String message, String reason) {
        messages.add(message + "\n - Reason: " + reason);
    }

    @HideFromJS
    public List<String> getMessages() {
        return messages;
    }
}
